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

import org.codegeny.jakartron.dbunit.DBUnit;
import org.codegeny.jakartron.dbunit.DBUnitConnection;
import org.codegeny.jakartron.jpa.PersistenceUnitDefinition.Property;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.dbunit.database.DatabaseConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.sql.DataSourceDefinition;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.Nonbinding;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static javax.persistence.spi.PersistenceUnitTransactionType.JTA;

@ExtendWithJakartron
@DataSourceDefinition(name = "mydb", className = "org.h2.jdbcx.JdbcDataSource", minPoolSize = 1, maxPoolSize = 2, url = "jdbc:h2:mem:mydb")
@PersistenceUnitDefinition(unitName = "tests", jtaDataSourceName = "mydb", transactionType = JTA, managedClasses = President.class, properties = {
        @Property(name = "javax.persistence.schema-generation.database.action", value = "create"),
        @Property(name = "hibernate.dialect", value = "org.hibernate.dialect.H2Dialect")
})
@DBUnitConnection(jndi = "mydb", properties = {
        @DBUnitConnection.Property(name = DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, value = "false"),
        @DBUnitConnection.Property(name = DatabaseConfig.PROPERTY_DATATYPE_FACTORY, value = "org.dbunit.ext.h2.H2DataTypeFactory"),
})
public class JPADBUnitWithParameterInjectionTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface TestData {

        @Nonbinding
        long value();
    }

    @ApplicationScoped
    public static class TestDataProvider {

        @PersistenceContext(unitName = "tests")
        private EntityManager entityManager;

        @Produces
        @TestData(0)
        public President president(InjectionPoint injectionPoint) {
            return entityManager.find(President.class, injectionPoint.getAnnotated().getAnnotation(TestData.class).value());
        }
    }

    @Test
    @Transactional
    @DBUnit(initialDataSets = "presidents.xml", expectedDataSets = "expected.xml")
    public void test(@TestData(1L) Provider<President> president) {
        // If we want a managed entity (not detached), we have to use Provider<T> because, otherwise, the entity will be loaded
        // before the transaction starts (through the @Transactional interceptor).
        // There is no JUnit interceptor which encompasses the parameters resolving step AND the test method invocation with those parameters.
        // Provider.get() can be safely called multiple times (no need to cache the value in a variable) because as long as the EntityManaged
        // remains open (which is the case for TransactionScoped EM while the TX is active), the same object will always be returned
        Assertions.assertNotNull(president.get());
        Assertions.assertEquals("G. Washington", president.get().getName());
        president.get().setName("B. Obama");
    }
}
