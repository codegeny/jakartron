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

import org.codegeny.jakartron.servlet.Initialized;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.faces.component.UIInput;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

@Dependent
public class JSFProducer {

    public void configure(@Observes @Initialized ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        context.setInitParameter(UIInput.EMPTY_STRING_AS_NULL_PARAM_NAME, Boolean.TRUE.toString());
        context.addServlet("faces", FacesServlet.class).addMapping("*.xhtml");
    }
}
