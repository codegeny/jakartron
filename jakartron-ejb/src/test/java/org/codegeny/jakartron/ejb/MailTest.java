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

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.awaitility.Awaitility;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.mail.ra.MailListener;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@ExtendWithJakartron
public class MailTest {

    @RegisterExtension
    public static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.ALL);

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "mailServer", propertyValue = "localhost"),
            @ActivationConfigProperty(propertyName = "mailFolder", propertyValue = "INBOX"),
            @ActivationConfigProperty(propertyName = "userName", propertyValue = "bar@example.com"),
            @ActivationConfigProperty(propertyName = "password", propertyValue = "secret-pwd"),
            @ActivationConfigProperty(propertyName = "pollingInterval", propertyValue = "1000"),
            @ActivationConfigProperty(propertyName = "port", propertyValue = "3143")
    })
    public static class MyMDB implements MailListener {

        private static volatile boolean received = false;

        @Override
        public void onMessage(Message message) {
            try {
                Assertions.assertEquals("some body", message.getContent());
                received = true;
            } catch (IOException | MessagingException exception) {
                throw new EJBException(exception);
            }
        }
    }

    @Test
    public void test() {
        GREEN_MAIL.setUser("bar@example.com", "bar@example.com", "secret-pwd");
        GreenMailUtil.sendTextEmailTest("bar@example.com", "foo@example.com", "some subject", "some body");
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> MyMDB.received);
    }
}
