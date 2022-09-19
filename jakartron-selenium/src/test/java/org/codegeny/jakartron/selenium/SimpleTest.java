package org.codegeny.jakartron.selenium;

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

        @FindBy(id = "foo")
        private WebElement foo;

        @FindBy(id = "bar")
        private WebElement bar;

        @FindBy(id = "submit")
        private WebElement submit;

        public MyForm(WebDriver driver) {
            this.driver = driver;
        }

        public WebElement getFoo() {
            return foo;
        }

        public WebElement getBar() {
            return bar;
        }

        public WebElement getSubmit() {
            return submit;
        }

        public MyPage submit(String foo, String bar) {
            this.foo.clear();
            this.foo.sendKeys(foo);
            this.bar.clear();
            this.bar.sendKeys(bar);
            submit.submit();
            return FragmentablePageFactory.createPage(driver, MyPage.class);
        }
    }

    public static class MyPage {

        @FindBy(id = "form")
        private MyForm myForm;

        public MyForm getForm() {
            return myForm;
        }
    }

    @Test
    public void test(@Page("index.html") MyPage myPage) {
        Assertions.assertEquals("FOO", myPage.getForm().getFoo().getAttribute("value"));
        Assertions.assertEquals("BAR", myPage.getForm().getBar().getAttribute("value"));
    }
}
