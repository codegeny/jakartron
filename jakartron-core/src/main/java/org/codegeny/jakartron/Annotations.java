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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Annotations {

    @FunctionalInterface
    public interface Expander {

        Expander SUPER_CLASS = annotated -> Stream.of(annotated).flatMap(asStream(Class.class)).map(Class::getSuperclass).filter(Objects::nonNull);
        Expander INTERFACES = annotated -> Stream.of(annotated).flatMap(asStream(Class.class)).map(Class::getInterfaces).flatMap(Stream::of);
        Expander PACKAGE = annotated -> Stream.of(annotated).flatMap(asStream(Class.class)).map(Class::getPackage);
        Expander ENCLOSING = annotated -> Stream.of(annotated).flatMap(asStream(Class.class)).map(Class::getEnclosingClass).filter(Objects::nonNull);
        Expander DECLARING = annotated -> Stream.of(annotated).flatMap(asStream(Member.class)).map(Member::getDeclaringClass);
        Expander META_ANNOTATIONS = annotated -> Stream.of(annotated.getAnnotations()).map(Annotation::annotationType);
        Expander OVERRIDES = annotated -> Stream.of(annotated).flatMap(asStream(Method.class))
                .filter(method -> !Modifier.isStatic(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers()))
                .flatMap(method -> Stream.concat(
                                        Stream.of(method.getDeclaringClass().getSuperclass()).filter(Objects::nonNull),
                                        Stream.of(method.getDeclaringClass().getInterfaces())
                                )
                                .flatMap(type -> Stream.of(type.getMethods())
                                        .filter(m -> !Modifier.isStatic(m.getModifiers()) && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isFinal(m.getModifiers()))
                                        .filter(m -> m.getName().equals(method.getName()))
                                        .filter(m -> Arrays.equals(m.getParameterTypes(), method.getParameterTypes()))
                                )
                );

        static Expander of(Expander... expanders) {
            return Stream.of(expanders).reduce((a, b) -> annotatedElement -> Stream.concat(a.expand(annotatedElement), b.expand(annotatedElement))).orElseGet(() -> annotatedElement -> Stream.empty());
        }

        Stream<? extends AnnotatedElement> expand(AnnotatedElement annotatedElement);
    }

    public static Stream<AnnotatedElement> expand(AnnotatedElement element, Expander... expanders) {
        return StreamSupport.stream(spliterator(element, Expander.of(expanders)), false);
    }

    public static <A extends Annotation> Stream<A> findAnnotations(AnnotatedElement element, Class<A> annotationType, Expander... expanders) {
        return expand(element, expanders).flatMap(current -> Stream.of(current.getAnnotationsByType(annotationType)));
    }

    private static Spliterator<AnnotatedElement> spliterator(AnnotatedElement element, Expander expander) {
        return GraphIterator.breadthFirst(element, expander::expand).asSpliterator();
    }

    private static final class GraphIterator<T> implements Iterator<T> {

        public static <T> GraphIterator<T> breadthFirst(T seed, Function<? super T, ? extends Stream<? extends T>> expander) {
            return new GraphIterator<>(seed, expander, true);
        }

        public static <T> GraphIterator<T> depthFirst(T seed, Function<? super T, ? extends Stream<? extends T>> expander) {
            return new GraphIterator<>(seed, expander, false);
        }

        private final Function<? super T, ? extends Stream<? extends T>> expander;
        private final Deque<T> deque = new ArrayDeque<>();
        private final Set<? super T> processed = new HashSet<>();
        private final Consumer<? super T> consumer;

        private GraphIterator(T seed, Function<? super T, ? extends Stream<? extends T>> expander, boolean breadthFirst) {
            this.expander = expander;
            this.consumer = breadthFirst ? deque::addLast : deque::addFirst;
            this.consumer.accept(seed);
        }

        @Override
        public boolean hasNext() {
            return !deque.isEmpty();
        }

        @Override
        public T next() {
            T next = deque.remove();
            expander.apply(next).filter(processed::add).forEach(consumer);
            return next;
        }

        public Spliterator<T> asSpliterator() {
            return Spliterators.spliterator(this, Long.MAX_VALUE, 0);
        }
    }

    public static <S, T> Function<S, T> as(Class<T> klass) {
        return source -> klass.isInstance(source) ? klass.cast(source) : null;
    }

    public static <S, T> Function<S, Stream<T>> asStream(Class<T> klass) {
        return source -> Stream.of(source).filter(klass::isInstance).map(klass::cast);
    }

    public static <S, T> Function<S, Optional<T>> asOptional(Class<T> klass) {
        return source -> Optional.of(source).filter(klass::isInstance).map(klass::cast);
    }

    private Annotations() {
        throw new InternalError("Can't instantiate utility class");
    }
}
