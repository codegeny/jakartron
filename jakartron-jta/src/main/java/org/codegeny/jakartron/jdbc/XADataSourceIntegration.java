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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.apache.commons.dbcp2.managed.BasicManagedDataSource;
import org.codegeny.jakartron.jndi.JNDI;
import org.kohsuke.MetaInfServices;

import javax.annotation.sql.DataSourceDefinition;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@MetaInfServices
public final class XADataSourceIntegration implements Extension {

    private static final Logger LOGGER = Logger.getLogger(XADataSourceIntegration.class.getName());

    private final Set<DataSourceDefinition> dataSources = new HashSet<>();

    public void process(@Observes @WithAnnotations(DataSourceDefinition.class) ProcessAnnotatedType<?> event) {
        dataSources.addAll(event.getAnnotatedType().getAnnotations(DataSourceDefinition.class));
    }

    public void makeDataSourceInjectable(@Observes AfterBeanDiscovery event) {
        dataSources.forEach(dataSource -> event.<BasicDataSource>addBean()
                .types(Object.class, DataSource.class, XADataSource.class)
                .produceWith(instance -> createDataSource(dataSource, instance))
                .destroyWith((instance, context) -> closeDataSource(instance))
                .qualifiers(JNDI.Literal.of(dataSource.name()))
                .scope(Singleton.class)
        );
    }

    private void closeDataSource(BasicDataSource dataSource) {
        try {
            dataSource.close();
        } catch (Exception exception) {
            LOGGER.log(Level.WARNING, exception, () -> "Error while closing pool");
        }
    }

    private BasicDataSource createDataSource(DataSourceDefinition definition, Instance<Object> instance) {

        Map<String, Object> map = new HashMap<>();
        for (String property : definition.properties()) {
            int index = property.indexOf('=');
            if (index != -1) {
                map.put(property.substring(0, index), property.substring(index + 1));
            }
        }
        map.put("url", definition.url());
        map.put("name", definition.name());
        map.put("password", definition.password());
        map.put("databaseName", definition.databaseName());
        map.put("serverName", definition.serverName());
        map.put("portNumber", definition.portNumber());
        map.put("loginTimeout", definition.loginTimeout());

        try {
            Class<?> klass = Thread.currentThread().getContextClassLoader().loadClass(definition.className());
            if (definition.transactional()) {
                if (!XADataSource.class.isAssignableFrom(klass)) {
                    throw new RuntimeException("DataSource is transactional but does not implement XADataSource");
                }
                XADataSource xaDataSource = klass.asSubclass(XADataSource.class).newInstance();
                BeanUtils.copyProperties(xaDataSource, map);
                BasicManagedDataSource pool = new BasicManagedDataSource();
                pool.setXaDataSourceInstance(xaDataSource);
                pool.setTransactionManager(instance.select(TransactionManager.class).get());
                pool.setTransactionSynchronizationRegistry(instance.select(TransactionSynchronizationRegistry.class).get());
                pool.setMinIdle(definition.minPoolSize());
                pool.setMaxIdle(definition.maxPoolSize());
                pool.setInitialSize(definition.initialPoolSize());
                return pool;
            } else {
                if (!DataSource.class.isAssignableFrom(klass)) {
                    throw new RuntimeException("");
                }
                DataSource dataSource = klass.asSubclass(DataSource.class).newInstance();
                BeanUtils.copyProperties(dataSource, map);
                BasicDataSource pool = new BasicDataSource() {

                    @Override
                    protected ConnectionFactory createConnectionFactory() {
                        return new DataSourceConnectionFactory(dataSource);
                    }
                };
                pool.setMinIdle(definition.minPoolSize());
                pool.setMaxIdle(definition.maxPoolSize());
                pool.setInitialSize(definition.initialPoolSize());
                return pool;
            }
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RuntimeException("Can't create a data source for " + definition, exception);
        }
    }
}
