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
package org.openengsb.core.util;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * provides additional util-methods for Collections not present in commons-collections or guava
 */
public final class CollectionUtilsExtended {

    /**
     * provides a view to the given collection, that only contains objects that are instances of the given clazz. It
     * uses the guava-methods {@link Collections2#filter(Collection, Predicate)} and
     * {@link Collections2#transform(Collection, Function)}.
     *
     * The returned collection is immutable.
     */
    public static <TargetType> Collection<TargetType> filterCollectionByClass(Collection<?> source,
            final Class<TargetType> clazz) {
        Collection<?> filtered = Collections2.filter(source, new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return clazz.isInstance(input);
            }
        });
        return Collections2.transform(filtered, new Function<Object, TargetType>() {
            @SuppressWarnings("unchecked")
            @Override
            public TargetType apply(Object input) {
                return (TargetType) input;
            }
        });
    }

    private CollectionUtilsExtended() {
    }

}
