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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public final class CollectionUtils2 {

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

    private CollectionUtils2() {
    }

}
