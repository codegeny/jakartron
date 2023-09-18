package org.codegeny.jakartron.junit;

import org.codegeny.jakartron.DisableDiscovery;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@ExtendWithJakartron
@DisableDiscovery
public class NestedTestTest {

    @Test
    public void simpleTest() {
        System.out.println("simple");
    }

    @Nested
    public class NestedTest {

        @Test
        public void nested() {
            System.out.println("nested");
        }
    }
}
