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
import java.util.*;
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

    private static Stream<AnnotatedElement> expand(AnnotatedElement element, Set<Mode> modes) {
        return StreamSupport.stream(Spliterators.spliterator(new AnnotatedElementIterator(element, modes), Long.MAX_VALUE, 0), false);
    }

    private static <A extends Annotation> Stream<A> getAnnotations(AnnotatedElement current, Class<A> annotationType) {
        return Stream.concat(
                current.isAnnotationPresent(annotationType) ? Stream.of(current.getAnnotation(annotationType)) : Stream.empty(),
                Stream.of(current.getAnnotationsByType(annotationType))
        );
    }

    public static <A extends Annotation> Stream<A> findAnnotations(AnnotatedElement element, Class<A> annotationType, Mode... modes) {
        return expand(element, new LinkedHashSet<>(Arrays.asList(modes))).flatMap(current -> getAnnotations(current, annotationType));
    }

    private Annotations() {
        throw new InternalError("Can't instantiate utility class");
    }

    private static class AnnotatedElementIterator implements Iterator<AnnotatedElement> {

        private final Set<Mode> modes;
        private final Deque<AnnotatedElement> deque = new ArrayDeque<>();
        private final Set<AnnotatedElement> visited = new HashSet<>();

        AnnotatedElementIterator(AnnotatedElement seed, Set<Mode> modes) {
            this.modes = modes;
            this.deque.add(seed);
        }

        @Override
        public boolean hasNext() {
            return !deque.isEmpty();
        }

        @Override
        public AnnotatedElement next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            AnnotatedElement next = deque.remove();
            modes.stream().flatMap(mode -> mode.get(next)).filter(visited::add).forEach(deque::add);
            return next;
        }
    }
}
