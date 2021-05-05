package org.codegeny.jakartron.jta;

/*-
 * #%L
 * jakartron-jta
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

import javax.enterprise.util.AnnotationLiteral;
import javax.transaction.Transactional;

public final class TransactionalLiteral extends AnnotationLiteral<Transactional> implements Transactional {

    /**
     * @see java.io.Serializable
     */
    private static final long serialVersionUID = 0x776f726b666c6f77L;

    private final TxType value;

    public TransactionalLiteral(TxType value) {
        this.value = value;
    }

    @Override
    public TxType value() {
        return value;
    }

    @Override
    public Class<?>[] rollbackOn() {
        return new Class[0];
    }

    @Override
    public Class<?>[] dontRollbackOn() {
        return new Class[0];
    }
}
