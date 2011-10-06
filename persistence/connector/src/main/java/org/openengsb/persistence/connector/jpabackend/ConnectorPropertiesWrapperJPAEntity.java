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
/**
* Licensed to the Austrian Association for Software Tool Integration (AASTI)
* under one or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information regarding copyright
* ownership. The AASTI licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.openengsb.persistence.connector.jpabackend;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.persistence.PersistenceException;

@Entity(name = "CONNECTOR_PROPERTIES_WRAPPER")
public class ConnectorPropertiesWrapperJPAEntity {

    @Id
    @GeneratedValue
    @Column(name = "ID")
    private Long id;
    @Column(name = "COLLECTION_TYPE", length = 127)
    private String collectionType;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @OrderColumn
    private List<ConnectorPropertyJPAEntity> properties;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    public List<ConnectorPropertyJPAEntity> getProperties() {
        return properties;
    }

    public void setProperties(List<ConnectorPropertyJPAEntity> properties) {
        this.properties = properties;
    }

    @SuppressWarnings("unchecked")
    public static ConnectorPropertiesWrapperJPAEntity getFromObject(Object property) {
        Class<?> clazz = property.getClass();

        ConnectorPropertiesWrapperJPAEntity wrapper = new ConnectorPropertiesWrapperJPAEntity();

        List<ConnectorPropertyJPAEntity> propList = new ArrayList<ConnectorPropertyJPAEntity>();
        wrapper.setProperties(propList);
        wrapper.setCollectionType(clazz.getName());
        Object[] arr;
        if (clazz.isArray()) {
            Class<?> compClass = clazz.getComponentType();
            if (compClass.isPrimitive()) {
                compClass = ClassUtils.primitiveToWrapper(compClass);
                int length = Array.getLength(property);
                Object wrapperArray = Array.newInstance(compClass, length);
                for (int i = 0; i < length; i++) {
                    Array.set(wrapperArray, i, Array.get(property, i));
                }
                arr = (Object[]) wrapperArray;
            } else {
                arr = (Object[]) property;

            }
            loopProperties(Arrays.asList(arr), propList);
            return wrapper;
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            Collection<Object> coll = (Collection<Object>) property;
            loopProperties(coll, propList);
            return wrapper;
        }

        wrapper.setCollectionType(null);
        propList.add(ConnectorPropertyJPAEntity.getFromObject(property));
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    public Object toObject() throws PersistenceException {
        if (collectionType == null) {
            ConnectorPropertyJPAEntity entity = properties.toArray(new ConnectorPropertyJPAEntity[1])[0];
            return entity.toObject();
        }
        try {
            Class<?> collectionClass = Class.forName(collectionType);
            if (collectionClass.isArray()) {
                Object arr = Array.newInstance(collectionClass.getComponentType(), properties.size());
                int i = 0;
                for (ConnectorPropertyJPAEntity entity : properties) {
                    Array.set(arr, i, entity.toObject());
                    i++;
                }
                return arr;
            } else {
                Collection<Object> collection = (Collection<Object>) collectionClass.newInstance();
                for (ConnectorPropertyJPAEntity entity : properties) {
                    collection.add(entity.toObject());
                }
                return collection;
            }
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    private static void loopProperties(Iterable<Object> iter, List<ConnectorPropertyJPAEntity> propSet) {
        Iterator<Object> iterator = iter.iterator();

        while (iterator.hasNext()) {
            Object obj = iterator.next();
            ConnectorPropertyJPAEntity entity = ConnectorPropertyJPAEntity.getFromObject(obj);
            propSet.add(entity);
        }
    }

}
