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

import org.codegeny.jakartron.jpa.PersistenceUnitDefinition.Property;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import static javax.persistence.spi.PersistenceUnitTransactionType.JTA;

@ExtendWithJakartron
@DataSourceDefinition(name = "mydb", className = "org.h2.jdbcx.JdbcDataSource", minPoolSize = 1, maxPoolSize = 2, url = "jdbc:h2:mem:mydb")
@PersistenceUnitDefinition(unitName = "tests", jtaDataSourceName = "mydb", transactionType = JTA, managedClasses = President.class, properties = {
        @Property(name = "javax.persistence.schema-generation.database.action", value = "create"),
        @Property(name = "hibernate.dialect", value = "org.hibernate.dialect.H2Dialect")
})
public class JPAJTADBTest {

    @PersistenceContext(unitName = "tests")
    private EntityManager entityManager;

    @Inject
    private UserTransaction transaction;

    @Resource(lookup = "mydb")
    private DataSource dataSource;

    @Test
    public void test() throws Exception {

//        transaction.begin();
        Assertions.assertEquals(0, entityManager.createNamedQuery("countPresidents", Number.class).getSingleResult().intValue());
//        transaction.rollback();

        transaction.begin();
        entityManager.persist(new President("G. Washington"));
        entityManager.persist(new President("A. Lincoln"));
        entityManager.flush();
        transaction.commit();

        transaction.begin();
        Assertions.assertEquals(2, entityManager.createNamedQuery("countPresidents", Number.class).getSingleResult().intValue());
        transaction.rollback();

        transaction.begin();
        entityManager.persist(new President("T. Roosevelt"));
        entityManager.flush();
        transaction.rollback();

        transaction.begin();
        Assertions.assertEquals(2, entityManager.createNamedQuery("countPresidents", Number.class).getSingleResult().intValue());
        transaction.rollback();
    }
}
