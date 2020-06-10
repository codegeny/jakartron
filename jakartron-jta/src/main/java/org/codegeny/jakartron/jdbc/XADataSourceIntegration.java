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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.codegeny.jakartron.jndi.JNDI;
import org.kohsuke.MetaInfServices;

import javax.annotation.sql.DataSourceDefinition;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.util.HashSet;
import java.util.Set;

@MetaInfServices
public final class XADataSourceIntegration implements Extension {

    private final Set<DataSourceDefinition> dataSources = new HashSet<>();

    public void process(@Observes @WithAnnotations(DataSourceDefinition.class) ProcessAnnotatedType<?> event) {
        dataSources.addAll(event.getAnnotatedType().getAnnotations(DataSourceDefinition.class));
    }

    public void makeDataSourceInjectable(@Observes AfterBeanDiscovery event) {
        dataSources.forEach(dataSource -> event.<HikariDataSource>addBean()
                .types(Object.class, DataSource.class, XADataSource.class)
                .createWith(context -> createDataSource(dataSource))
                .destroyWith((instance, context) -> instance.close())
                .qualifiers(JNDI.Literal.of(dataSource.name()))
                .scope(Singleton.class)
        );
    }

    private HikariDataSource createDataSource(DataSourceDefinition definition) {
        HikariConfig config = new HikariConfig();
        config.setPoolName(definition.name());
        if (!definition.url().isEmpty()) {
            config.setJdbcUrl(definition.url());
        }
        if (!definition.user().isEmpty()) {
            config.setUsername(definition.user());
        }
        if (!definition.password().isEmpty()) {
            config.setPassword(definition.password());
        }
        if (definition.maxPoolSize() >= 0) {
            config.setMaximumPoolSize(definition.maxPoolSize());
        }
        if (definition.maxIdleTime() >= 0) {
            config.setIdleTimeout(definition.maxIdleTime());
        }
        return new HikariDataSource(config);
    }
}
