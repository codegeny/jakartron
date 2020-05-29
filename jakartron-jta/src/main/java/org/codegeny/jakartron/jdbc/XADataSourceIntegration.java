package org.codegeny.jakartron.jdbc;

/*-
 * #%L
 * jakartron-jta
 * %%
 * Copyright (C) 2018 - 2020 Codegeny
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

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.kohsuke.MetaInfServices;

import javax.annotation.Priority;
import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.*;
import javax.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator;
import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

@MetaInfServices
public final class XADataSourceIntegration implements Extension {

    private final Set<PoolingDataSource> dataSources = new HashSet<>();

    public void process(@Observes @WithAnnotations(DataSourceDefinition.class) ProcessAnnotatedType<?> event) {
        event.getAnnotatedType().getAnnotations(DataSourceDefinition.class).forEach(this::createDataSource);
    }

    public void processResources(@Observes @WithAnnotations(Resource.class) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType()
                .filterFields(f -> f.isAnnotationPresent(Resource.class) && DataSource.class.isAssignableFrom(f.getJavaMember().getType()))
                .forEach(this::processField);
    }

    private void processField(AnnotatedFieldConfigurator<?> f) {
        Resource resource = f.getAnnotated().getAnnotation(Resource.class);
        String name = resource.lookup();
        if (name.isEmpty()) {
            name = f.getAnnotated().getJavaMember().getName();
        }
        f.add(InjectLiteral.INSTANCE).add(NamedLiteral.of(name));
    }

    public void makeDataSourceInjectable(@Observes AfterBeanDiscovery event) {
        dataSources.forEach(dataSource -> event.addBean()
                .beanClass(PoolingDataSource.class)
                .types(DataSource.class)
                .qualifiers(NamedLiteral.of(dataSource.getUniqueName()))
                .produceWith(context -> dataSource)
                .scope(ApplicationScoped.class)
        );
    }

    public void afterDeploymentValidation(@Observes @Priority(0) AfterDeploymentValidation event) {
        dataSources.forEach(PoolingDataSource::init);
    }

    public void beforeShutdown(@Observes BeforeShutdown event) {
        dataSources.forEach(PoolingDataSource::close);
    }

    private void createDataSource(DataSourceDefinition definition) {
        PoolingDataSource dataSource = new PoolingDataSource();
        dataSource.setUniqueName(definition.name());
        dataSource.setMinPoolSize(definition.minPoolSize());
        dataSource.setMaxPoolSize(definition.maxPoolSize());
        dataSource.setPreparedStatementCacheSize(50);
        dataSource.setAllowLocalTransactions(definition.transactional());
        dataSource.setClassName(definition.className());
        dataSource.getDriverProperties().put("url", definition.url());
        dataSource.getDriverProperties().put("user", definition.user());
        dataSource.getDriverProperties().put("password", definition.password());
        dataSources.add(dataSource);
    }
}
