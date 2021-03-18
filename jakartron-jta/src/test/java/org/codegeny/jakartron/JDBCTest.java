package org.codegeny.jakartron;

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

import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@DataSourceDefinition(name = "java:/jdbc/mydb", className = "org.h2.jdbcx.JdbcDataSource", minPoolSize = 5, maxPoolSize = 25, url = "jdbc:h2:mem:mydb")
@ExtendWithJakartron
public class JDBCTest {

    @Resource(lookup = "java:/jdbc/mydb")
    private DataSource dataSource;

    @Inject
    private TransactionManager transactionManager;

    @Test
    public void test() throws Exception {

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("create table test ( name varchar2(50) )")) {
                statement.execute();
            }
            try (PreparedStatement statement = connection.prepareStatement("select count(*) from test")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    Assertions.assertTrue(resultSet.next());
                    Assertions.assertEquals(0, resultSet.getInt(1));
                }
            }
        }

        transactionManager.begin();
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("insert into test values ('hello')")) {
                statement.execute();
            }
            try (PreparedStatement statement = connection.prepareStatement("select count(*) from test")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    Assertions.assertTrue(resultSet.next());
                    Assertions.assertEquals(1, resultSet.getInt(1));
                }
            }
        } finally {
            transactionManager.rollback();
        }


        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select count(*) from test")) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    Assertions.assertTrue(resultSet.next());
                    Assertions.assertEquals(0, resultSet.getInt(1));
                }
            }
        }
    }
}
