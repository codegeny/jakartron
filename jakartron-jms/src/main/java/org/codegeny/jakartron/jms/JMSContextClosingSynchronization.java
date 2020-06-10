package org.codegeny.jakartron.jms;

/*-
 * #%L
 * jakartron-jms
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


import com.arjuna.ats.jta.logging.jtaLogger;

import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;
import javax.transaction.Synchronization;

public class JMSContextClosingSynchronization implements Synchronization  {


    private final JMSContext context;

    public JMSContextClosingSynchronization(JMSContext context) {
        this.context = context;
    }

    @Override
    public void beforeCompletion() {
        // Nothing to do
    }

    @Override
    public void afterCompletion(int status) {
        if (jtaLogger.logger.isTraceEnabled()) {
            jtaLogger.logger.trace("Closing context " + context);
        }

        try {
            context.close();
        } catch (JMSRuntimeException e) {
            jtaLogger.i18NLogger.warn_failed_to_close_jms_connection(context.toString(), e);
        }
    }

}
