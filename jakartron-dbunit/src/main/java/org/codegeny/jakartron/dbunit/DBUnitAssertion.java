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

import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.*;
import org.dbunit.operation.DatabaseOperation;

import java.sql.SQLException;
import java.util.stream.Stream;

public abstract class DBUnitAssertion extends DatabaseOperation {

    @Override
    public final void execute(IDatabaseConnection connection, IDataSet expectedDataSet) throws DatabaseUnitException, SQLException {
        assertEquals(expectedDataSet, connection.createDataSet());
    }

    protected abstract void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException, SQLException;

    public static final class NonStrict extends DBUnitAssertion {

        @Override
        protected void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
            for (String tableName : expectedDataSet.getTableNames()) {
                ITable expectedTable = expectedDataSet.getTable(tableName);
                ITable actualTable = actualDataSet.getTable(tableName);
                ITableMetaData expectedMetaData = expectedTable.getTableMetaData();
                ITableMetaData actualMetaData = actualTable.getTableMetaData();
                String[] ignoredColumns = Stream.of(Columns.getColumnDiff(expectedMetaData, actualMetaData).getActual()).map(Column::getColumnName).toArray(String[]::new);
                Assertion.assertEqualsIgnoreCols(expectedTable, actualTable, ignoredColumns);
            }
        }
    }

    public static final class NonStrictUnordered extends DBUnitAssertion {

        @Override
        protected void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
            for (String tableName : expectedDataSet.getTableNames()) {
                ITable expectedTable = expectedDataSet.getTable(tableName);
                Column[] expectedColumns = expectedTable.getTableMetaData().getColumns();
                ITable expectedSortedTable = new SortedTable(expectedTable, expectedColumns);
                ITable actualSortedTable = new SortedTable(actualDataSet.getTable(tableName), expectedColumns);
                ITableMetaData expectedMetaData = expectedSortedTable.getTableMetaData();
                ITableMetaData actualMetaData = actualSortedTable.getTableMetaData();
                String[] ignoredColumns = Stream.of(Columns.getColumnDiff(expectedMetaData, actualMetaData).getActual()).map(Column::getColumnName).toArray(String[]::new);
                Assertion.assertEqualsIgnoreCols(expectedSortedTable, actualSortedTable, ignoredColumns);
            }
        }
    }

    public static final class Strict extends DBUnitAssertion {

        @Override
        protected void assertEquals(IDataSet expectedDataSet, IDataSet actualDataSet) throws DatabaseUnitException {
            Assertion.assertEquals(expectedDataSet, actualDataSet);
        }
    }
}

