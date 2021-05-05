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

import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;

public final class QualifierInstance<A extends Annotation> {

    private final A qualifier;
    private final BeanManager beanManager;

    public QualifierInstance(A qualifier, BeanManager beanManager) {
        this.qualifier = qualifier;
        this.beanManager = beanManager;
    }

    public A getQualifier() {
        return qualifier;
    }

    @Override
    public int hashCode() {
        return beanManager.getQualifierHashCode(qualifier);
    }

    @Override
    public boolean equals(Object that) {
        return super.equals(that) || that instanceof QualifierInstance<?> && equals((QualifierInstance<?>) that);
    }

    private boolean equals(QualifierInstance<?> that) {
        return beanManager.areQualifiersEquivalent(qualifier, that.qualifier);
    }
}
