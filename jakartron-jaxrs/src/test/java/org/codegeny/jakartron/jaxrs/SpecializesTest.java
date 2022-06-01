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

import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.Vetoed;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Set;

@ExtendWithJakartron
public class SpecializesTest {

    @ApplicationPath("/api")
    public static class MyApplication extends Application {

        @Override
        public Set<Object> getSingletons() {
            return Collections.singleton(new MyResource("foo"));
        }
    }

    @Specializes
    @ApplicationPath("/api")
    public static class MyApplication2 extends MyApplication {

        @Override
        public Set<Object> getSingletons() {
            return Collections.singleton(new MyResource("bar"));
        }
    }

    @Path("/")
    @Vetoed
    public static class MyResource {

        private final String content;

        public MyResource(String content) {
            this.content = content;
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String get() {
            return content;
        }

    }

    @Test
    public void test(@Base("/api") WebTarget target) {
        assert "bar".equals(target.request().get(String.class));
    }
}
