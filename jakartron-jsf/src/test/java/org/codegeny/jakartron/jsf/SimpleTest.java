package org.codegeny.jakartron.jsf;

/*-
 * #%L
 * jakartron-jsf
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

import org.codegeny.jakartron.junit.EnableCDI;
import org.codegeny.jakartron.servlet.Base;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;

@EnableCDI
public class SimpleTest {

    public static class MyBeanPage {

        public MyBeanPage(WebDriver driver) {
            this.driver = driver;
        }

        private final WebDriver driver;

        @FindBy(how = How.ID, id = "q")
        private WebElement q;

        @FindBy(how = How.ID, id = "form:input")
        private WebElement input;

        @FindBy(how = How.ID, id = "form:submit")
        private WebElement submit;

        public String getQ() {
            return q.getText();
        }

        public MyBeanPage submit(String q) {
            input.clear();
            input.sendKeys(q);
            submit.submit();
            return PageFactory.initElements(driver, MyBeanPage.class);
        }
    }

    @Test
    public void test(@Base("my-bean.xhtml") String uri) {
        WebDriver driver = new HtmlUnitDriver();
        driver.get(uri);

        MyBeanPage page = PageFactory.initElements(driver, MyBeanPage.class);
        Assertions.assertEquals("hello", page.getQ());

        page = page.submit("world");
        Assertions.assertEquals("world", page.getQ());
    }
}