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

import org.dbunit.operation.DatabaseOperation;
import org.dbunit.operation.DeleteAllOperation;
import org.dbunit.operation.InsertOperation;
import org.dbunit.operation.UpdateOperation;
import org.codegeny.jakartron.dbunit.DBUnitAssertion.*;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Dependent
public class DBUnitProducer {

    @Produces
    @Singleton
    public UpdateOperation update() {
        return (UpdateOperation) DatabaseOperation.UPDATE;
    }

    @Produces
    @Singleton
    public InsertOperation insert() {
        return (InsertOperation) DatabaseOperation.INSERT;
    }

    @Produces
    @Singleton
    public DeleteAllOperation deleteAllOperation() {
        return (DeleteAllOperation) DatabaseOperation.DELETE_ALL;
    }

    @Produces
    @Singleton
    public NonStrict nonStrict() {
        return new NonStrict();
    }

    @Produces
    @Singleton
    public NonStrictUnordered nonStrictUnordered() {
        return new NonStrictUnordered();
    }

    @Produces
    @Singleton
    public Strict strict() {
        return new Strict();
    }
}
