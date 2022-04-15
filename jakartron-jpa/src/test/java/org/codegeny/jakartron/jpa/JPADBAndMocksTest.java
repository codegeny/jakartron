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

import org.codegeny.jakartron.DisableDiscovery;
import org.codegeny.jakartron.jpa.PersistenceUnitDefinition.Property;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.codegeny.jakartron.mockito.EnableAutoMocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.sql.DataSourceDefinition;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import static javax.persistence.spi.PersistenceUnitTransactionType.JTA;

@ExtendWithJakartron
@DisableDiscovery
@EnableJPA
@EnableAutoMocks
@DataSourceDefinition(name = "mydb", className = "org.h2.jdbcx.JdbcDataSource", url = "jdbc:h2:mem:mydb")
@PersistenceUnitDefinition(unitName = "tests", jtaDataSourceName = "mydb", transactionType = JTA, managedClasses = President.class, properties = {
        @Property(name = "javax.persistence.schema-generation.database.action", value = "create"),
        @Property(name = "hibernate.show_sql", value = "false")
})
public class JPADBAndMocksTest {

    @ApplicationScoped
    @Transactional
    public static class MyRepository {

        @PersistenceContext(unitName = "tests")
        private EntityManager entityManager;

        public President save(President president) {
            return entityManager.merge(president);
        }
    }

    @Inject
    private MyRepository repository;

    @Test
    public void test() {
        Assertions.assertNotNull(repository.save(new President("G. Washington")).getId());
    }
}
