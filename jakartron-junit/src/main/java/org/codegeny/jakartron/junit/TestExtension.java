package org.codegeny.jakartron.junit;

/*-
 * #%L
 * jakartron-junit
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

import org.junit.jupiter.api.Nested;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.CreationException;
import javax.enterprise.inject.literal.InjectLiteral;
import javax.enterprise.inject.spi.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public final class TestExtension implements Extension {

    private final TestContext context = new TestContext();

    private final Class<?> testClass;

    private final Set<AnnotatedType<?>> nestedTestClasses = new HashSet<>();

    public TestExtension(Class<?> testClass) {
        this.testClass = testClass;
    }

    public void setPriority(@Observes AfterTypeDiscovery event) {
        for (Class<?> current = this.testClass; current != null; current = current.getEnclosingClass()) {
            event.getAlternatives().add(current);
        }
    }

    public void nestedTest(@Observes @WithAnnotations(Nested.class) ProcessAnnotatedType<?> event) {
        event.configureAnnotatedType().constructors().forEach(c -> c.add(InjectLiteral.INSTANCE));
        event.configureAnnotatedType().add(Dependent.Literal.INSTANCE);
        nestedTestClasses.add(event.getAnnotatedType());
        event.veto();
    }

//    public void processTestClass(@Observes @WithAnnotations(Testable.class) ProcessAnnotatedType<?> event) {
//        event.configureAnnotatedType().add(PriorityLiteral.DEFAULT);
//        testTypes.add(event.getAnnotatedType().getJavaClass());
//    }

    public void processTestAttributes(@Observes ProcessBeanAttributes<?> attributes) {
        for (Class<?> current = this.testClass; current != null; current = current.getEnclosingClass()) {
            if (attributes.getBeanAttributes().getTypes().contains(current)) {
                attributes.configureBeanAttributes()
                        .scope(TestScoped.class)
                        .alternative(true);
            }
        }
    }

    public void registerContext(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        //nestedTestClasses.stream().map(nestedTestType -> createNestedBean(nestedTestType, beanManager)).forEach(event::addBean);
        nestedTestClasses.forEach(nestedTestType -> addNestedTest(nestedTestType, event, beanManager));
        event.addContext(context);
    }

    private <X> Bean<?> createNestedBean(AnnotatedType<X> type, BeanManager beanManager) {
        return beanManager.createBean(beanManager.createBeanAttributes(type), type.getJavaClass(), beanManager.getInjectionTargetFactory(type));
    }

    private <X> void addNestedTest(AnnotatedType<X> type, AfterBeanDiscovery event, BeanManager beanManager) {
        event.addBean().read(type).createWith(creationalContext -> {
            Constructor<?> constructor = type.getJavaClass().getConstructors()[0];
            Object[] args = Stream.of(constructor.getParameters())
                    .map(Parameter::getType)
                    .map(t -> {
                        Bean<?> testBean = beanManager.resolve(beanManager.getBeans(t));
                        return beanManager.getReference(testBean, t, creationalContext);
                    })
                    .toArray();
            try {
                return (X) constructor.newInstance(args);
            } catch (Exception exception) {
                throw new CreationException(exception);
            }
        });
    }

    public void reset() {
        context.reset();
    }
}
