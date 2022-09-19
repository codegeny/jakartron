package org.codegeny.jakartron.junit;

/*-
 * #%L
 * jakartron-junit
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

import org.codegeny.jakartron.DisableDiscovery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ExtendWithJakartron
@DisableDiscovery
public class DecoratorTest {

    public interface Service {

        String get();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface Foo {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface Bar {
    }

    @Foo
    public static class FooService implements Service {

        @Override
        public String get() {
            return "foo";
        }
    }

    @Bar
    public static class BarService implements Service {

        @Override
        public String get() {
            return "bar";
        }
    }

    @Decorator
    public static class FooDecorator implements Service {

        @Inject
        @Foo
        @Delegate
        private Service delegate;

        @Override
        public String get() {
            return "FOO " + delegate.get();
        }
    }

    @Decorator
    public static class BarDecorator implements Service {

        @Inject
        @Bar
        @Delegate
        private Service delegate;

        @Override
        public String get() {
            return "BAR " + delegate.get();
        }
    }

    @Test
    public void test(@Foo Service foo, @Bar Service bar) {
        Assertions.assertEquals("FOO foo", foo.get());
        Assertions.assertEquals("BAR bar", bar.get());
    }
}
