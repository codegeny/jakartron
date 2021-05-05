package org.codegeny.jakartron.jpa;

/*-
 * #%L
 * jakartron-jpa
 * %%
 * Copyright (C) 2018 - 2021 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.codegeny.jakartron.QualifierInstance;
import org.kohsuke.MetaInfServices;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.*;
import javax.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.*;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.transaction.TransactionScoped;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Poorman's JPA integration which adds @Inject on fields annotated with @PersistenceContext or @PersistenceUnit and
 * turns those @PersistenceXXX annotations into qualifiers. For each found qualifier, a bean providing either an
 * EntityManagerFactory or an EntityManager is added to the bean manager.
 **/
@MetaInfServices
public final class JPAIntegration implements Extension {

    private final Set<QualifierInstance<PersistenceUnit>> persistenceUnits = new HashSet<>();
    private final Set<QualifierInstance<PersistenceContext>> persistenceContexts = new HashSet<>();
    private final Set<PersistenceUnitInfo> persistenceUnitInfos = new HashSet<>();

    public void addQualifiers(@Observes BeforeBeanDiscovery event) {
        event.addQualifier(PersistenceUnit.class);
        event.configureQualifier(PersistenceContext.class)
                .filterMethods(m -> m.getJavaMember().getReturnType().isArray())
                .forEach(m -> m.add(Nonbinding.Literal.INSTANCE));
    }

    public void defineUnits(@Observes @WithAnnotations(PersistenceUnitDefinition.class) ProcessAnnotatedType<?> event) {
        persistenceUnitInfos.add(new PersistenceUnitInfoImpl(event.getAnnotatedType().getAnnotation(PersistenceUnitDefinition.class)));
    }

    public void makeInjectable(@Observes @WithAnnotations({PersistenceUnit.class, PersistenceContext.class}) ProcessAnnotatedType<?> event, BeanManager beanManager) {
        event.configureAnnotatedType()
                .filterFields(f -> f.isAnnotationPresent(PersistenceUnit.class) || f.isAnnotationPresent(PersistenceContext.class))
                .forEach(f ->makeInjectable(f, beanManager));
    }

    private void makeInjectable(AnnotatedFieldConfigurator<?> fieldConfigurator, BeanManager beanManager) {
        PersistenceContext persistenceContext = fieldConfigurator.getAnnotated().getAnnotation(PersistenceContext.class);
        PersistenceUnit persistenceUnit = fieldConfigurator.getAnnotated().getAnnotation(PersistenceUnit.class);
        if (persistenceContext != null) {
            persistenceContexts.add(new QualifierInstance<>(persistenceContext, beanManager));
            persistenceUnit = new PersistenceUnitLiteral(persistenceContext);
        }
        persistenceUnits.add(new QualifierInstance<>(persistenceUnit, beanManager));
        fieldConfigurator.add(InjectLiteral.INSTANCE);
    }

    public void addBeans(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        persistenceUnits.forEach(persistenceUnit -> addPersistenceUnit(persistenceUnit.getQualifier(), event, beanManager));
        persistenceContexts.forEach(persistenceContext -> addPersistenceContext(persistenceContext.getQualifier(), event));
    }

    public void fireConfigurationEvent(@Observes @Priority(100) AfterDeploymentValidation event, BeanManager beanManager) {
        beanManager.getEvent().select(PersistenceUnitDefinitionEvent.class).fire(persistenceUnitInfos::add);
        if (!persistenceUnits.stream().allMatch(q -> beanManager.createInstance().select(EntityManagerFactory.class, q.getQualifier()).get().isOpen())) {
            event.addDeploymentProblem(new Exception("Cannot initialize some EMF"));
        }
    }

    private void addPersistenceUnit(PersistenceUnit persistenceUnit, AfterBeanDiscovery event, BeanManager beanManager) {
        event.<EntityManagerFactory>addBean()
                .createWith(context -> createEntityManagerFactory(persistenceUnit, beanManager))
                .destroyWith((entityManagerFactory, context) -> entityManagerFactory.close())
                .scope(ApplicationScoped.class)
                .types(Object.class, EntityManagerFactory.class)
                .qualifiers(persistenceUnit, Any.Literal.INSTANCE);
    }

    private EntityManagerFactory createEntityManagerFactory(PersistenceUnit persistenceUnit, BeanManager beanManager) {
        Map<?, ?> map = Collections.singletonMap("javax.persistence.bean.manager", beanManager);
        return Stream.concat(beanManager.createInstance().select(PersistenceUnitInfo.class).stream(), persistenceUnitInfos.stream())
                .filter(i -> i.getPersistenceUnitName().equals(persistenceUnit.unitName()))
                .findFirst()
                .map(unitInfo -> PersistenceProviderResolverHolder.getPersistenceProviderResolver()
                        .getPersistenceProviders().stream()
                        .findFirst()
                        .orElseThrow(IllegalStateException::new)
                        .createContainerEntityManagerFactory(unitInfo, map)
                )
                .orElseGet(() -> Persistence.createEntityManagerFactory(persistenceUnit.unitName(), map));
    }

    private void addPersistenceContext(PersistenceContext persistenceContext, AfterBeanDiscovery event) {
        Map<?, ?> properties = Stream.of(persistenceContext.properties()).collect(Collectors.toMap(PersistenceProperty::name, PersistenceProperty::value));
        event.<EntityManager>addBean()
                .produceWith(instance -> instance.select(EntityManagerFactory.class, new PersistenceUnitLiteral(persistenceContext)).get().createEntityManager(properties))
                .disposeWith((entityManager, instance) -> entityManager.close())
                .scope(toScope(persistenceContext.type()))
                .types(Object.class, EntityManager.class)
                .qualifiers(persistenceContext, Any.Literal.INSTANCE);
    }

    private static Class<? extends Annotation> toScope(PersistenceContextType type) {
        switch (type) {
            case TRANSACTION:
                return TransactionScoped.class;
            case EXTENDED:
                return Dependent.class;
            default:
                throw new InternalError();
        }
    }

    static class PersistenceUnitLiteral extends AnnotationLiteral<PersistenceUnit> implements PersistenceUnit {

        /**
         * @see java.io.Serializable
         */
        private static final long serialVersionUID = 0x776f726b666c6f77L;

        private final String name;
        private final String unitName;

        PersistenceUnitLiteral(PersistenceContext persistenceContext) {
            this.name = persistenceContext.name();
            this.unitName = persistenceContext.unitName();
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String unitName() {
            return unitName;
        }
    }

    static final class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

        private final PersistenceUnitDefinition definition;

        PersistenceUnitInfoImpl(PersistenceUnitDefinition definition) {
            this.definition = definition;
        }

        @Override
        public String getPersistenceUnitName() {
            return definition.unitName();
        }

        @Override
        public String getPersistenceProviderClassName() {
            return definition.providerClassName().isEmpty() ? null : definition.providerClassName();
        }

        @Override
        public PersistenceUnitTransactionType getTransactionType() {
            return definition.transactionType();
        }

        @Override
        public DataSource getJtaDataSource() {
            return dataSource(definition.jtaDataSourceName());
        }

        @Override
        public DataSource getNonJtaDataSource() {
            return dataSource(definition.nonJtaDataSourceName());
        }

        @Override
        public List<String> getMappingFileNames() {
            return Arrays.asList(definition.mappingFileNames());
        }

        @Override
        public List<URL> getJarFileUrls() {
            return Stream.of(definition.jarFileUrls()).map(this::newURL).collect(Collectors.toList());
        }

        @Override
        public URL getPersistenceUnitRootUrl() {
            return definition.unitRootUrl().isEmpty() ? null : newURL(definition.unitRootUrl());
        }

        @Override
        public List<String> getManagedClassNames() {
            return Stream.concat(
                    Stream.of(definition.managedClasses()).map(Class::getName),
                    Stream.of(definition.managedClassNames())
            ).collect(Collectors.toList());
        }

        @Override
        public boolean excludeUnlistedClasses() {
            return definition.excludeUnlistedClasses();
        }

        @Override
        public SharedCacheMode getSharedCacheMode() {
            return definition.sharedCacheMode();
        }

        @Override
        public ValidationMode getValidationMode() {
            return definition.validationMode();
        }

        @Override
        public Properties getProperties() {
            Properties properties = new Properties();
            Stream.of(definition.properties()).forEach(property -> properties.setProperty(property.name(), property.value()));
            return properties;
        }

        @Override
        public String getPersistenceXMLSchemaVersion() {
            return definition.xmlSchemaVersion();
        }

        @Override
        public ClassLoader getClassLoader() {
            return null;
        }

        @Override
        public void addTransformer(ClassTransformer transformer) {
        }

        @Override
        public ClassLoader getNewTempClassLoader() {
            return null;
        }

        private URL newURL(String url) {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private DataSource dataSource(String name) {
            if (name.isEmpty()) {
                return null;
            }
            try {
                Context context = new InitialContext();
                try {
                    return (DataSource) context.lookup(name);
                } finally {
                    context.close();
                }
            } catch (NamingException namingException) {
                throw new IllegalStateException("Problem looking up datasource " + name, namingException);
            }
        }
    }
}
