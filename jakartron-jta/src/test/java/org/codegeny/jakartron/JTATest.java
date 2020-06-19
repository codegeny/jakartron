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

import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.transaction.TransactionScoped;
import javax.transaction.Transactional;
import java.io.Serializable;

@ExtendWithJakartron
public class JTATest {

    @TransactionScoped
    public static class TransactionScopedBean implements Serializable {

        public String getMessage() {
            return "hello world!";
        }
    }

    private boolean transactionStated;

    @Test
    @Transactional
    public void test(TransactionScopedBean bean) {
        Assertions.assertTrue(transactionStated);
        Assertions.assertEquals("hello world!", bean.getMessage());
    }

    public void observe(@Observes @Initialized(TransactionScoped.class) Object object) {
        transactionStated = true;
    }
}
