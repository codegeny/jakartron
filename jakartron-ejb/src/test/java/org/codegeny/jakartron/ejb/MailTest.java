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

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.awaitility.Awaitility;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.wildfly.mail.ra.MailListener;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ExtendWithJakartron
public class MailTest {

    @MessageDriven(activationConfig = {
            @ActivationConfigProperty(propertyName = "mailServer", propertyValue = "localhost"),
            @ActivationConfigProperty(propertyName = "mailFolder", propertyValue = "INBOX"),
            @ActivationConfigProperty(propertyName = "userName", propertyValue = "bar@example.com"),
            @ActivationConfigProperty(propertyName = "password", propertyValue = "secret-pwd"),
            @ActivationConfigProperty(propertyName = "pollingInterval", propertyValue = "1000"),
            @ActivationConfigProperty(propertyName = "port", propertyValue = "3143")
    })
    public static class MyMDB implements MailListener {

        @Override
        public void onMessage(Message message) {
            try {
                Object content = message.getContent();
                if (content instanceof Multipart) {
                    Multipart multipart = (Multipart) content;
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart body = multipart.getBodyPart(i);
                        if ("some body".equals(body.getContent())) {
                            RECEIVED.set(true);
                            break;
                        }
                    }
                }
            } catch (IOException | MessagingException exception) {
                throw new EJBException(exception);
            }
        }
    }

    @RegisterExtension
    public static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.ALL)
            .withConfiguration(new GreenMailConfiguration().withUser("bar@example.com", "bar@example.com", "secret-pwd"));

    static final AtomicBoolean RECEIVED = new AtomicBoolean();

    @Test
    public void test() throws Exception {
        MimeBodyPart text = new MimeBodyPart();
        text.setText("some body");
        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(text);
        GreenMailUtil.sendMessageBody("bar@example.com", "foo@example.com", "some subject", multipart, null, ServerSetupTest.SMTP);

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(RECEIVED);
    }
}
