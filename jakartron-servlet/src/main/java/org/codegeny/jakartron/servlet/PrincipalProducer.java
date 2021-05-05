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

import org.codegeny.jakartron.security.PrincipalHolder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

public class PrincipalProducer {

    public void observe(@Observes @Initialized(ApplicationScoped.class) ServletContext context, PrincipalHolder holder) {
        context.addListener(new ServletRequestListener() {

            @Override
            public void requestDestroyed(ServletRequestEvent event) {
                holder.setPrincipal(null);
            }

            @Override
            public void requestInitialized(ServletRequestEvent event) {
                ServletRequest request = event.getServletRequest();
                if (request instanceof HttpServletRequest) {
                    holder.setPrincipal(((HttpServletRequest) request).getUserPrincipal());
                }
            }
        });
    }
}
