package org.codegeny.jakartron.jaxrs;

/*-
 * #%L
 * jakartron-jaxrs
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
import org.codegeny.jakartron.servlet.Base;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;

import static io.restassured.RestAssured.given;

@ExtendWithJakartron
public class ResourceTest {

    @Path("foo")
    public static class MyResource {

        @Path("bar")
        @GET
        public Response ok() {
            return Response.ok("hello world!").build();
        }
    }

    @ApplicationPath("api")
    public static class MyApplication extends Application {

        @Override
        public Set<Class<?>> getClasses() {
            return Collections.singleton(MyResource.class);
        }
    }

    @Test
    public void test(@Base("api") String baseUri, @Base("baz.txt") String baz) {
        given().baseUri(baseUri)
                .when().get("foo/bar")
                .then().body(Matchers.is("hello world!"));

        given().baseUri(baz)
                .when().get()
                .then().body(Matchers.is("baz"));
    }
}
