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

package org.openengsb.core.persistence.internal;

import java.io.File;
import java.util.List;

/**
 * Represents an internal, very simplified index to store/search the types and locations of stored objects. Be careful!
 * This implementation is not syncronised. In case you need this class in a multthreaded context make sure of this
 * behavior yourself!
 */
public interface PersistenceIndex {

    /**
     * Index an serialized bean object by all it's classes. Keep in mind that you need to update the index afterwards
     * again using the {@link #updateIndex()} method.
     */
    void indexObject(Class<?> bean, File beanLocation);

    /**
     * removes an object from the index. Keep in mind that you need to update the index afterwards again using the
     * {@link #updateIndex()} method.
     */
    void removeIndexObject(ObjectInfo info);

    /**
     * Writes the index to a file. You need to call this method after each change to the index; otherwise your change
     * wont be persisted.
     */
    void updateIndex() ;

    /**
     * Retrieves all index objects containing the class given as parameter.
     */
    List<ObjectInfo> findIndexObject(Class<?> beanClass);

}
