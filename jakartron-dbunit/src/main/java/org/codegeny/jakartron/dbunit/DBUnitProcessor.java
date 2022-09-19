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

import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.csv.CsvURLDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.CompositeOperation;
import org.dbunit.operation.DatabaseOperation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class DBUnitProcessor {

    @Inject
    private Instance<DatabaseOperation> operation;

    @Inject
    @Any
    private Instance<IDatabaseConnection> connection;

    public void before(Method method) {
        try {
            for (DBUnit dbUnit : dbUnits(method)) {
                IDatabaseConnection connection = getConnection(dbUnit.connection());
                try {
                    IDatabaseTester tester = new DefaultDatabaseTester(connection);
                    tester.setDataSet(getDataSet(method.getDeclaringClass(), dbUnit.initialDataSets()));
                    tester.setSetUpOperation(getOperation(dbUnit.setUpOperations()));
                    tester.onSetup();
                } finally {
                    connection.close();
                }
            }
        } catch (Exception exception) {
            throw new DatabaseUnitRuntimeException(exception);
        }
    }

    public void after(Method method) {
        try {
            for (DBUnit dbUnit : dbUnits(method)) {
                IDatabaseConnection connection = getConnection(dbUnit.connection());
                try {
                    IDatabaseTester tester = new DefaultDatabaseTester(connection);
                    tester.setDataSet(getDataSet(method.getDeclaringClass(), dbUnit.expectedDataSets()));
                    tester.setTearDownOperation(getOperation(dbUnit.tearDownOperations()));
                    tester.onTearDown();
                } finally {
                    connection.close();
                }
            }
        } catch (Exception exception) {
            throw new DatabaseUnitRuntimeException(exception);
        }
    }

    private List<DBUnit> dbUnits(Method method) {
        List<DBUnit> result = new ArrayList<>();
        result.addAll(Arrays.asList(method.getDeclaringClass().getAnnotationsByType(DBUnit.class)));
        result.addAll(Arrays.asList(method.getAnnotationsByType(DBUnit.class)));
        return result;
    }

    private IDatabaseConnection getConnection(String connectionName) {
        return this.connection.select(NamedLiteral.of(connectionName)).get();
    }

    private IDataSet getDataSet(Class<?> testClass, String[] dataSetNames) throws DataSetException, IOException {
        IDataSet[] dataSets = new IDataSet[dataSetNames.length];
        for (int i = 0; i < dataSets.length; i++) {
            dataSets[i] = getDataSet(testClass, dataSetNames[i]);
        }
        return new CompositeDataSet(dataSets);
    }

    private IDataSet getDataSet(Class<?> testClass, String dataSetName) throws DataSetException, IOException {
        if (dataSetName.endsWith("/")) {
            return new CsvURLDataSet(testClass.getResource(dataSetName));
        }
        if (endsWithIgnoreCase(dataSetName, ".xml")) {
            return new FlatXmlDataSetBuilder().setColumnSensing(true).build(testClass.getResourceAsStream(dataSetName));
        }
        if (endsWithIgnoreCase(dataSetName, ".xls")) {
            return new XlsDataSet(testClass.getResourceAsStream(dataSetName));
        }
        throw new DataSetException("Unknown dataset type: " + dataSetName);
    }

    private DatabaseOperation getOperation(Class<? extends DatabaseOperation>[] operationClasses) {
        DatabaseOperation[] operations = new DatabaseOperation[operationClasses.length];
        for (int i = 0; i < operations.length; i++) {
            operations[i] = operation.select(operationClasses[i]).get();
        }
        return new CompositeOperation(operations);
    }

    private static boolean endsWithIgnoreCase(String string, String suffix) {
        return string.regionMatches(true, string.length() - suffix.length(), suffix, 0, suffix.length());
    }
}
