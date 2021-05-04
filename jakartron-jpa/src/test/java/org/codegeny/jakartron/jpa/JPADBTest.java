package org.codegeny.jakartron.jpa;

/*-
 * #%L
 * jakartron-jpa
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

import org.codegeny.jakartron.jpa.PersistenceUnitDefinition.Property;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.sql.DataSourceDefinition;
import javax.persistence.*;

import static javax.persistence.spi.PersistenceUnitTransactionType.RESOURCE_LOCAL;

@ExtendWithJakartron
@DataSourceDefinition(name = "mydb", className = "org.h2.jdbcx.JdbcDataSource", minPoolSize = 1, maxPoolSize = 2, url = "jdbc:h2:mem:mydb")
@PersistenceUnitDefinition(unitName = "tests", nonJtaDataSourceName = "mydb", transactionType = RESOURCE_LOCAL, managedClasses = President.class, properties = {
        @Property(name = "javax.persistence.schema-generation.database.action", value = "create")
//        @Property(name = "hibernate.dialect", value = "org.hibernate.dialect.H2Dialect")
})
public class JPADBTest {

    @PersistenceContext(unitName = "tests", type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;

    @Test
    public void test() {
        entityManager.getTransaction().begin();
        entityManager.persist(new President("G. Washington"));
        entityManager.persist(new President("A. Lincoln"));
        entityManager.getTransaction().commit();

        entityManager.clear();

        entityManager.getTransaction().begin();
        Assertions.assertEquals(2, entityManager.createNamedQuery("countPresidents", Number.class).getSingleResult().intValue());
        entityManager.getTransaction().rollback();
    }
}
