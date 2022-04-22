package org.codegeny.jakartron;

import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.codegeny.jakartron.mockito.EnableAutoMocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ExtendWithJakartron
@EnableAutoMocks
public class ScopeTest {

    public interface MyDependency {

        String hello();
    }

    @ApplicationScoped
    public static class MyService {

        @Inject
        private MyDependency dependency;

        public String hello() {
            return dependency.hello() + " world!";
        }
    }

    @Inject
    private MyService service;

    @Inject
    private MyDependency dependency;

    @Test
    public void test() {
        Mockito.when(dependency.hello()).thenReturn("HELLO");
        Assertions.assertEquals("HELLO world!", service.hello());
        Mockito.verify(dependency, Mockito.times(1)).hello();
    }

    @Test
    public void test2() {
        Mockito.when(dependency.hello()).thenReturn("HELLO");
        Assertions.assertEquals("HELLO world!", service.hello());
        Mockito.verify(dependency, Mockito.times(1)).hello();
    }
}
