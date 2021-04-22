package org.codegeny.jakartron.dbunit;

/*-
 * #%L
 * jakartron-dbunit
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

import org.codegeny.jakartron.jndi.JNDI;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.kohsuke.MetaInfServices;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.*;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

@MetaInfServices
public class DBUnitExtension implements Extension {

    private final Set<DBUnitConnection> connections = new HashSet<>();

    public void processAnnotatedType(@Observes @WithAnnotations(DBUnitConnection.class) ProcessAnnotatedType<?> event) {

        connections.add(event.getAnnotatedType().getAnnotation(DBUnitConnection.class));
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        connections.forEach(connection -> event.<IDatabaseConnection>addBean()
                .types(IDatabaseConnection.class)
                .qualifiers(Any.Literal.INSTANCE, NamedLiteral.of(connection.name()))
                .produceWith(instance -> produce(connection, instance))
                .disposeWith((c, instance) -> dispose(c))
                .scope(ApplicationScoped.class)
        );
    }

    private IDatabaseConnection produce(DBUnitConnection connection, Instance<Object> instance) {
        try {
            DataSource dataSource = instance.select(DataSource.class, JNDI.Literal.of(connection.jndi())).get();
            IDatabaseConnection result = new DatabaseDataSourceConnection(dataSource, connection.schema().isEmpty() ? null : connection.schema());
            result.getConfig().setPropertiesByString(Stream.of(connection.properties()).collect(toProperties(DBUnitConnection.Property::name, DBUnitConnection.Property::value)));
            return result;
        } catch (SQLException | DatabaseUnitException exception) {
            throw new CreationException("Can't create IDatabaseConnection", exception);
        }
    }

    private <T> Collector<T, ?, Properties> toProperties(Function<? super T, String> keyMapper, Function<? super T, String> valueMapper) {
        return Collector.of(Properties::new, (properties, element) -> properties.setProperty(keyMapper.apply(element), valueMapper.apply(element)), (a, b) -> {
            a.putAll(b);
            return a;
        });
    }

    private void dispose(IDatabaseConnection connection) {
        try {
            connection.close();
        } catch (SQLException sqlException) {
            throw new InjectionException("Can't dispose connection");
        }
    }
}
