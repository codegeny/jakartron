package org.codegeny.jakartron;

/*-
 * #%L
 * jakartron-core
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

import javax.enterprise.inject.Produces;
import javax.enterprise.util.AnnotationLiteral;

public final class ProducesLiteral extends AnnotationLiteral<Produces> implements Produces {

    /**
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 0x776f726b666c6f77L;

    public static final Produces INSTANCE = new ProducesLiteral();

    private ProducesLiteral() {
        super();
    }
}
