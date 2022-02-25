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

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.is;

@ExtendWithJakartron
public class ResourceTest {

    @Path("foo/bar")
    public static class Bar {

        @GET
        public Response bar() {
            return Response.ok("bar").build();
        }
    }

    @ApplicationPath("api")
    public static class MyApplication extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            return singleton(Bar.class);
        }
    }

    @Test
    public void test(@Base("api") String base) {
        given().baseUri(base).when().get("foo/bar").then().body(is("bar"));
    }
}
