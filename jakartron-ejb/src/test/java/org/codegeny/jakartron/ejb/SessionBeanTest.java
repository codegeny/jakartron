package org.codegeny.jakartron.ejb;

/*-
 * #%L
 * jakartron-ejb
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

@ExtendWithJakartron
public class SessionBeanTest {

    @Stateless
    public static class MyBean {

        @Inject
        private TransactionManager tm;

        public boolean run() {
            try {
                return tm.getStatus() == Status.STATUS_ACTIVE;
            } catch (SystemException exception) {
                return false;
            }
        }
    }

    @Inject
    private MyBean myBean;

    @Test
    public void test() {
        Assertions.assertTrue(myBean.run());
    }
}
