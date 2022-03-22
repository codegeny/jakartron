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
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class Annotations {

    public enum Mode {

        SUPER_CLASS {
            @Override
            Stream<AnnotatedElement> get(AnnotatedElement annotatedElement) {
                return annotatedElement instanceof Class<?>
                        ? Stream.<AnnotatedElement>of(((Class<?>) annotatedElement).getSuperclass()).filter(Objects::nonNull)
                        : Stream.empty();
            }
        },
        INTERFACES {
            @Override
            Stream<AnnotatedElement> get(AnnotatedElement annotatedElement) {
                return annotatedElement instanceof Class<?>
                        ? Stream.<AnnotatedElement>of(((Class<?>) annotatedElement).getInterfaces()).filter(Objects::nonNull)
                        : Stream.empty();
            }
        },
        META_ANNOTATIONS {
            @Override
            Stream<AnnotatedElement> get(AnnotatedElement annotatedElement) {
                return Stream.of(annotatedElement.getAnnotations()).map(Annotation::annotationType);
            }
        },
        OVERRIDES {
            @Override
            Stream<AnnotatedElement> get(AnnotatedElement annotatedElement) {
                if (annotatedElement instanceof Method) {
                    Method method = (Method) annotatedElement;
                    if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers())) {
                        Class<?> declaringClass = method.getDeclaringClass();
                        Class<?> superClass = declaringClass.getSuperclass();
                        return Stream.concat(
                                superClass == null ? Stream.empty() : Stream.of(superClass),
                                Stream.of(declaringClass.getInterfaces())
                        ).flatMap(type -> Stream.of(type.getMethods())
                                .filter(m -> !Modifier.isStatic(m.getModifiers()) && !Modifier.isPrivate(m.getModifiers()) && !Modifier.isFinal(m.getModifiers()))
                                .filter(m -> m.getName().equals(method.getName()))
                                .filter(m -> Arrays.equals(m.getParameterTypes(), method.getParameterTypes()))
                        );
                    }
                }
                return Stream.empty();
            }
        },
        PACKAGE {
            @Override
            Stream<AnnotatedElement> get(AnnotatedElement annotatedElement) {
                return annotatedElement instanceof Class<?>
                        ? Stream.of(((Class<?>) annotatedElement).getPackage())
                        : Stream.empty();
            }
        },
        ENCLOSING {
            @Override
            Stream<AnnotatedElement> get(AnnotatedElement annotatedElement) {
                return annotatedElement instanceof Class<?>
                        ? Stream.of(((Class<?>) annotatedElement).getEnclosingClass())
                        : Stream.empty();
            }
        },
        DECLARING {
            @Override
            Stream<AnnotatedElement> get(AnnotatedElement annotatedElement) {
                return annotatedElement instanceof Member
                        ? Stream.of(((Member) annotatedElement).getDeclaringClass())
                        : Stream.empty();
            }
        };

        abstract Stream<AnnotatedElement> get(AnnotatedElement annotatedElement);
    }

    public static Stream<AnnotatedElement> expand(AnnotatedElement element, Mode... modes) {
        return StreamSupport.stream(spliterator(element, new LinkedHashSet<>(Arrays.asList(modes))), false);
    }

    public static <A extends Annotation> Stream<A> findAnnotations(AnnotatedElement element, Class<A> annotationType, Mode... modes) {
        return findAnnotations(element, annotationType::isInstance, modes).map(annotationType::cast);
    }

    public static Stream<Annotation> findAnnotations(AnnotatedElement element, Predicate<? super Annotation> predicate, Mode... modes) {
        return expand(element, modes).flatMap(current -> getAnnotations(current, predicate));
    }

    private static Stream<Annotation> getAnnotations(AnnotatedElement current, Predicate<? super Annotation> predicate) {
        return Stream.of(current.getAnnotations()).filter(predicate);
    }

    private static Spliterator<AnnotatedElement> spliterator(AnnotatedElement element, Set<Mode> modes) {
        return new GraphIterator<AnnotatedElement>(element, next -> modes.stream().flatMap(mode -> mode.get(next))).asSpliterator();
    }

    private static final class GraphIterator<T> implements Iterator<T> {

        enum SearchMode {

            BREADTH_FIRST {
                @Override
                <T> Consumer<T> forDeque(Deque<T> deque) {
                    return deque::add;
                }
            },
            DEPTH_FIRST {
                @Override
                <T> Consumer<T> forDeque(Deque<T> deque) {
                    return deque::addFirst;
                }
            };

            abstract <T> Consumer<T> forDeque(Deque<T> deque);
        }

        private final Function<T, Stream<? extends T>> expander;
        private final Deque<T> deque = new ArrayDeque<>();
        private final Set<T> visited = new HashSet<>();
        private final Consumer<T> consumer;

        GraphIterator(T seed, Function<T, Stream<? extends T>> expander) {
            this(seed, expander, SearchMode.BREADTH_FIRST);
        }

        GraphIterator(T seed, Function<T, Stream<? extends T>> expander, SearchMode mode) {
            this.expander = expander;
            this.deque.add(seed);
            this.consumer = mode.forDeque(deque);
        }

        @Override
        public boolean hasNext() {
            return !deque.isEmpty();
        }

        @Override
        public T next() {
            T next = deque.remove();
            expander.apply(next).filter(visited::add).forEach(consumer);
            return next;
        }

        public Spliterator<T> asSpliterator() {
            return Spliterators.spliterator(this, Long.MAX_VALUE, 0);
        }
    }

    private Annotations() {
        throw new InternalError("Can't instantiate utility class");
    }
}
