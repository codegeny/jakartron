package org.codegeny.jakartron;

/*-
 * #%L
 * jakartron-core
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

import javax.enterprise.inject.spi.BeanManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class BeanContract {

    private final Type type;
    private final Set<QualifierInstance<?>> qualifierInstances;
    private final Set<Annotation> qualifiers;

    public BeanContract(Type type, Set<Annotation> qualifiers, BeanManager beanManager) {
        this.type = type;
        this.qualifiers = qualifiers;
        this.qualifierInstances = qualifiers.stream().map(a -> new QualifierInstance<>(a, beanManager)).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object that) {
        return super.equals(that) || that instanceof BeanContract && equals((BeanContract) that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, qualifiers);
    }

    private boolean equals(BeanContract that) {
        return type.equals(that.type) && qualifierInstances.equals(that.qualifierInstances);
    }

    public Type getType() {
        return type;
    }

    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public String toString() {
        return type.getTypeName() + getQualifiers();
    }
}
