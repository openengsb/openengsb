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

package org.openengsb.core.edbi.api;

/**
 * An IndexField represents the mapping between a Classes property and the table column that specifies the index for the
 * Class.
 */
public interface IndexField<T> {

    /**
     * The Index this column belongs to.
     * 
     * @return an Index
     */
    Index<?> getIndex();

    /**
     * Returns the Class property name, usable as a bean accessor.
     * 
     * @return the name of the class property
     */
    String getName();

    /**
     * Returns the Class property type.
     * 
     * @return the type of the property
     * @throws UnavailableTypeInformationException if the type information is not available and can not be determined
     */
    Class<T> getType() throws UnavailableTypeInformationException;

    /**
     * Returns the mapped name, i.e. the name of the column in the Index table.
     * 
     * @return the mapped name
     */
    String getMappedName();

    /**
     * Returns an object representing the type of the native data structure that holds this IndexField.
     * 
     * @return an object representing a type.
     */
    Object getMappedType();

    /**
     * Allows you to specify a ClassLoader that is used when restoring type information when calling {@link #getType()}
     * on a sparsely loaded IndexField.
     * 
     * @param classLoader the class loader to use
     */
    void setClassLoader(ClassLoader classLoader);
}
