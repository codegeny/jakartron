package org.codegeny.jakartron.jsf;

/*-
 * #%L
 * jakartron-jsf
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

import org.codegeny.jakartron.AdditionalClasses;
import org.codegeny.jakartron.DisableDiscovery;
import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.codegeny.jakartron.selenium.EnableSelenium;
import org.codegeny.jakartron.selenium.Location;
import org.codegeny.jakartron.selenium.Navigator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@ExtendWithJakartron
@DisableDiscovery
@EnableJSF
@EnableSelenium
@EnableOmnifaces
@AdditionalClasses(MyBean.class)
public class SimpleTest {

    public static class MyForm {

        @FindBy(id = "form:input")
        private WebElement input;

        @FindBy(id = "form:submit")
        private WebElement submit;

        public void submit(String message) {
            input.clear();
            input.sendKeys(message);
            submit.submit();
        }
    }

    @Location("my-bean.xhtml")
    public static class MyPage {

        @FindBy(id = "message")
        private WebElement message;

        @FindBy(id = "form")
        private MyForm myForm;

        public MyForm getMyForm() {
            return myForm;
        }

        public String getMessage() {
            return message.getText();
        }
    }

    @Test
    public void test(Navigator navigator) {
        MyPage page = navigator.navigateTo(MyPage.class);
        Assertions.assertEquals("hello", page.getMessage());

        page.getMyForm().submit("world");
        page = navigator.current(MyPage.class);
        Assertions.assertEquals("world", page.getMessage());
    }
}

