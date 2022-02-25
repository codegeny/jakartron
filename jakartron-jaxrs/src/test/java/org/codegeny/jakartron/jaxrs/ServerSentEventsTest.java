package org.codegeny.jakartron.jaxrs;

/*-
 * #%L
 * jakartron-jaxrs
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
import org.codegeny.jakartron.servlet.Base;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import javax.ws.rs.sse.SseEventSource;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@ExtendWithJakartron
public class ServerSentEventsTest {

    @ApplicationScoped
    @Path("/my")
    public static class MyResource {

        @Resource(lookup = "java:comp/concurrent/ThreadPool")
        private ManagedScheduledExecutorService executorService;

        @GET
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void get(@Context Sse sse, @Context SseEventSink sink) {
            SseBroadcaster broadcaster = sse.newBroadcaster();
            broadcaster.register(sink);
            executorService.scheduleAtFixedRate(() -> broadcaster.broadcast(sse.newEventBuilder().data(String.class, "hello").build()), 1, 1, TimeUnit.SECONDS);
        }
    }

    @ApplicationPath("/api")
    public static class MyApplication extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            return Collections.singleton(MyResource.class);
        }
    }

    @Test
    public void test(@Base("/api/my") SseEventSource source) {
        AtomicInteger count = new AtomicInteger();
        source.register(event -> count.incrementAndGet());
        source.open();
        await().atMost(5, TimeUnit.SECONDS).untilAtomic(count, equalTo(3));
    }
}
