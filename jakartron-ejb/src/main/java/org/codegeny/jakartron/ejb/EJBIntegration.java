package org.codegeny.jakartron.ejb;

/*-
 * #%L
 * jakartron-ejb
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

import org.codegeny.jakartron.jta.TransactionalLiteral;
import org.kohsuke.MetaInfServices;

import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.transaction.Transactional;

// TODO support @Stateful
// TODO support @TransactionManagement
// TODO support EJBContext
@MetaInfServices
public final class EJBIntegration implements Extension {

    public void process(@Observes @WithAnnotations({Stateless.class, Singleton.class}) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType()
//                .remove(Stateless.class::isInstance)
//                .remove(Singleton.class::isInstance)
//                .remove(MessageDriven.class::isInstance)
                .add(() -> ActivateRequestContext.class)
                .add(ApplicationScoped.Literal.INSTANCE)
                .add(transactionLiteral(event.getAnnotatedType().getAnnotation(TransactionAttribute.class)))
                .filterMethods(m -> m.isAnnotationPresent(TransactionAttribute.class))
                .forEach(m -> m.add(transactionLiteral(m.getAnnotated().getAnnotation(TransactionAttribute.class))));
    }

    private static TransactionalLiteral transactionLiteral(TransactionAttribute attribute) {
        return attribute == null ? new TransactionalLiteral(Transactional.TxType.REQUIRED) : transactionLiteral(attribute.value());
    }

    private static TransactionalLiteral transactionLiteral(TransactionAttributeType type) {
        return new TransactionalLiteral(Transactional.TxType.valueOf(type.name()));
    }
}
