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

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Repeatable(DBUnits.class)
@InterceptorBinding
public @interface DBUnit {

    @Nonbinding
    String connection() default "";

    @Nonbinding
    Class<? extends DatabaseOperation>[] setUpOperations() default {DeleteAllOperation.class, InsertOperation.class};

    @Nonbinding
    Class<? extends DatabaseOperation>[] tearDownOperations() default {DBUnitAssertion.NonStrict.class};

    @Nonbinding
    String[] initialDataSets() default {};

    @Nonbinding
    String[] expectedDataSets() default {};
}
