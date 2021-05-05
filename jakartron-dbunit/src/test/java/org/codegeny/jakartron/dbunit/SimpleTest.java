package org.codegeny.jakartron.dbunit;

/*-
 * #%L
 * jakartron-dbunit
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

import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.dbunit.database.DatabaseConfig;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.annotation.sql.DataSourceDefinition;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@ExtendWithJakartron
@DataSourceDefinition(name = "java:/jdbc/test", className = "org.h2.jdbcx.JdbcDataSource", url = "jdbc:h2:mem:test")
@DBUnitConnection(jndi = "java:/jdbc/test", properties = @DBUnitConnection.Property(name = DatabaseConfig.PROPERTY_DATATYPE_FACTORY, value = "org.dbunit.ext.h2.H2DataTypeFactory"))
public class SimpleTest {

    @Resource(lookup = "java:/jdbc/test")
    private DataSource dataSource;

    @Test
    @DBUnit(expectedDataSets = "expected.xml")
    public void test() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("create table thing ( name varchar2 )");
            statement.execute("insert into thing values ('foo')");
            statement.execute("insert into thing values ('bar')");
        }
    }
}
