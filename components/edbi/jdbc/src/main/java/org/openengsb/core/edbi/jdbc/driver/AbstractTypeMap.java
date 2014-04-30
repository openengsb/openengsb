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
package org.openengsb.core.edbi.jdbc.driver;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.edbi.jdbc.api.TypeMap;
import org.openengsb.core.edbi.jdbc.sql.DataType;

/**
 * Abstract implementation of a TypeMap that can be used for convenient sub-classing.
 */
public abstract class AbstractTypeMap implements TypeMap {

    protected Map<Class<?>, DataType> map;

    public AbstractTypeMap() {
        map = new HashMap<>();

        initMap();
    }

    /**
     * Entry hook for filling the map. Use {@link #put(Class, int, String)} to add entries.
     */
    protected abstract void initMap();

    /**
     * Create a new DataType instance for the given type and name, and place it in the map with the given Class as key.
     * 
     * @param clazz the key
     * @param type the type of {@code java.sql.Types}
     * @param name the name of the type corresponding to the used dbms
     * @return the DataType created
     */
    protected DataType put(Class<?> clazz, int type, String name) {
        DataType dataType = new DataType(type, name);
        map.put(clazz, dataType);
        return dataType;
    }

    @Override
    public DataType getType(Class<?> javaType) {
        return map.get(javaType);
    }

}
