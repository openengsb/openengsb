/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.core.common.util;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;

public final class CollectionUtilsExtended {

    public static <T> Collection<T> filterCollectionByClass(Collection<?> source, final Class<T> clazz) {
        Collection<?> filtered = Collections2.filter(source, new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return clazz.isInstance(input);
            }
        });
        return Collections2.transform(filtered, new Function<Object, T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T apply(Object input) {
                return (T) input;
            }
        });
    }

    public <K, T> Function<K, T> autoInitializingFunction(Class<? extends T> elementClass) {
        return new InitCollectionFunction<K, T>(elementClass);
    }

    public <K, T> Map<K, T> makeAutoInitializingMap(Class<? extends T> elementClass) {
        return new MapMaker().makeComputingMap(autoInitializingFunction(elementClass));
    }

    static class InitCollectionFunction<K, T> implements Function<K, T> {

        private Class<? extends T> elementClass;

        public InitCollectionFunction(Class<? extends T> elementClass) {
            this.elementClass = elementClass;
        }

        @Override
        public T apply(K input) {
            try {
                return elementClass.newInstance();
            } catch (InstantiationException e) {
                throw new ComputationException(e);
            } catch (IllegalAccessException e) {
                throw new ComputationException(e);
            }
        }

    }

    private CollectionUtilsExtended() {
    }

}
