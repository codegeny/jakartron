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

import org.dbunit.DefaultDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.CompositeOperation;
import org.dbunit.operation.DatabaseOperation;

import javax.annotation.Priority;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Priority(1)
@Interceptor
@DBUnit
public class DBUnitInterceptor {

    @Inject
    private Instance<DatabaseOperation> operation;

    @Inject
    @Any
    private Instance<IDatabaseConnection> connection;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
        List<DBUnit> dbUnits = getBindings(invocationContext).stream().flatMap(this::extract).distinct().collect(Collectors.toList());
        for (DBUnit dbUnit : dbUnits) {
            IDatabaseConnection connection = getConnection(invocationContext.getTarget(), dbUnit.connection());
            try {
                IDatabaseTester tester = new DefaultDatabaseTester(connection);
                tester.setDataSet(getDataSet(invocationContext.getTarget(), dbUnit.initialDataSets()));
                tester.setSetUpOperation(getOperation(dbUnit.setUpOperations()));
                tester.onSetup();
            } finally {
                connection.close();
            }
        }
        Object result = invocationContext.proceed();
        for (DBUnit dbUnit : dbUnits) {
            IDatabaseConnection connection = getConnection(invocationContext.getTarget(), dbUnit.connection());
            try {
                IDatabaseTester tester = new DefaultDatabaseTester(connection);
                tester.setDataSet(getDataSet(invocationContext.getTarget(), dbUnit.expectedDataSets()));
                tester.setTearDownOperation(getOperation(dbUnit.tearDownOperations()));
                tester.onTearDown();
            } finally {
                connection.close();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    /** @link https://issues.jboss.org/browse/CDI-468 */
    private Set<Annotation> getBindings(InvocationContext invocationContext) {
        return (Set<Annotation>) invocationContext.getContextData().get("org.jboss.weld.interceptor.bindings");
    }

    private Stream<DBUnit> extract(Annotation binding) {
        if (binding instanceof DBUnit) {
            return Stream.of((DBUnit) binding);
        }
        if (binding instanceof DBUnits) {
            return Stream.of(((DBUnits) binding).value());
        }
        return Stream.empty();
    }

    private IDatabaseConnection getConnection(Object source, String connectionName) {
        return this.connection.select(NamedLiteral.of(connectionName)).get();
    }

    private IDataSet getDataSet(Object source, String... dataSetNames) throws DataSetException {
        IDataSet[] dataSets = new IDataSet[dataSetNames.length];
        for (int i = 0; i < dataSets.length; i++) {
            dataSets[i] = new FlatXmlDataSetBuilder().setColumnSensing(true).build(source.getClass().getResourceAsStream(dataSetNames[i]));
        }
        return new CompositeDataSet(dataSets);
    }

    private DatabaseOperation getOperation(Class<? extends DatabaseOperation>[] operationClasses) {
        DatabaseOperation[] operations = new DatabaseOperation[operationClasses.length];
        for (int i = 0; i < operations.length; i++) {
            operations[i] = operation.select(operationClasses[i]).get();
        }
        return new CompositeOperation(operations);
    }
}
