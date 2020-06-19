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
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@DataSourceDefinition(name = "mydb", className = "org.h2.jdbcx.JdbcDataSource", minPoolSize = 5, maxPoolSize = 25, url = "jdbc:h2:mem:mydb")
@ExtendWithJakartron
public class JDBCTest {

    @Resource(lookup = "mydb")
    private DataSource dataSource;

    @Test
    public void test() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println(connection.getMetaData());
        }
    }
}
