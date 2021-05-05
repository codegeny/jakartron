package org.codegeny.jakartron.servlet;

/*-
 * #%L
 * jakartron-servlet
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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

@ExtendWithJakartron
public class ServletTest {

    @WebServlet(name = "test", urlPatterns = "/test")
    public static class MyServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.getWriter().append("hello world!").flush();
        }
    }

    @WebServlet(name = "myjsp", urlPatterns = "/jsp")
    public static class MyJSPServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            request.getRequestDispatcher("/page.jsp").forward(request, response);
        }
    }

    @Test
    public void test(@Base("/test") URL servletURL, @Base("/page.jsp") URL jspURL, @Base("/jsp") URL jsp2URL, @Base("foo.txt") URL foo) throws IOException {
        try (Scanner scanner = new Scanner(servletURL.openStream())) {
            Assertions.assertTrue(scanner.hasNextLine());
            Assertions.assertEquals("hello world!", scanner.nextLine());
        }

        try (Scanner scanner = new Scanner(jspURL.openStream())) {
            Assertions.assertTrue(scanner.hasNextLine());
            Assertions.assertEquals("<span>2</span>", scanner.nextLine());
        }

        try (Scanner scanner = new Scanner(jsp2URL.openStream())) {
            Assertions.assertTrue(scanner.hasNextLine());
            Assertions.assertEquals("<span>2</span>", scanner.nextLine());
        }

        try (Scanner scanner = new Scanner(foo.openStream())) {
            Assertions.assertTrue(scanner.hasNextLine());
            Assertions.assertEquals("foo", scanner.nextLine());
        }
    }
}
