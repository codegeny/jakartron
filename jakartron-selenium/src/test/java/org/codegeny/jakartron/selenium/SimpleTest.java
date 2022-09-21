package org.codegeny.jakartron.selenium;

/*-
 * #%L
 * jakartron-selenium
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@ExtendWithJakartron
public class SimpleTest {

    public static class MyForm {

        @FindBy(id = "foo")
        private WebElement foo;

        @FindBy(id = "bar")
        private WebElement bar;

        @FindBy(id = "submit")
        private WebElement submit;

        public WebElement getFoo() {
            return foo;
        }

        public WebElement getBar() {
            return bar;
        }

        public WebElement getSubmit() {
            return submit;
        }

        public void submit(String foo, String bar) {
            this.foo.clear();
            this.foo.sendKeys(foo);
            this.bar.clear();
            this.bar.sendKeys(bar);
            submit.submit();
        }
    }

    @Location("index.html")
    public static class HomePage {

        @FindBy(id = "form")
        private MyForm form;

        public MyForm getForm() {
            return form;
        }
    }

    @Test
    public void test(Navigator navigator) {
        HomePage home = navigator.navigateTo(HomePage.class);
        Assertions.assertEquals("FOO", home.getForm().getFoo().getAttribute("value"));
        Assertions.assertEquals("BAR", home.getForm().getBar().getAttribute("value"));
    }
}
