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

import org.codegeny.jakartron.junit.ExtendWithJakartron;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@ExtendWithJakartron
public class SimpleTest {

    public static class MyForm {

        private final WebDriver driver;

        @FindBy(id = "form:input")
        private WebElement input;

        @FindBy(id = "form:submit")
        private WebElement submit;

        public MyForm(WebDriver driver) {
            this.driver = driver;
        }

        public MyPage submit(String message) {
            input.clear();
            input.sendKeys(message);
            submit.submit();
            return FragmentablePageFactory.createPage(driver, MyPage.class);
        }
    }

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
    public void test(@Page("my-bean.xhtml") MyPage page) {
        Assertions.assertEquals("hello", page.getMessage());

        page = page.getMyForm().submit("world");
        Assertions.assertEquals("world", page.getMessage());
    }
}

