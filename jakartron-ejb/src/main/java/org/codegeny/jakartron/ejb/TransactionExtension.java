package org.codegeny.jakartron.ejb;

/*-
 * #%L
 * jakartron-ejb
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

import org.codegeny.jakartron.jta.TransactionalLiteral;
import org.kohsuke.MetaInfServices;

import javax.ejb.*;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.transaction.Transactional;

@MetaInfServices
public final class TransactionExtension implements Extension {

    public void transactional(@Observes @WithAnnotations({Stateless.class, Singleton.class, Stateful.class, MessageDriven.class}) ProcessAnnotatedType<?> event) {

        TransactionManagementType transactionManagementType = event.getAnnotatedType().isAnnotationPresent(TransactionManagement.class)
                ? event.getAnnotatedType().getAnnotation(TransactionManagement.class).value()
                : TransactionManagementType.CONTAINER;

        event.configureAnnotatedType().add(() -> ActivateRequestContext.class);

        if (transactionManagementType == TransactionManagementType.CONTAINER) {
            event.configureAnnotatedType()
                    .add(transactionLiteral(event.getAnnotatedType()))
                    .filterMethods(m -> m.isAnnotationPresent(TransactionAttribute.class))
                    .forEach(m -> m.add(transactionLiteral(m.getAnnotated())));
        }
    }

    private TransactionalLiteral transactionLiteral(Annotated annotated) {
        return new TransactionalLiteral(annotated.isAnnotationPresent(TransactionAttribute.class)
                ? Transactional.TxType.valueOf(annotated.getAnnotation(TransactionAttribute.class).value().name())
                : Transactional.TxType.REQUIRED
        );
    }
}
