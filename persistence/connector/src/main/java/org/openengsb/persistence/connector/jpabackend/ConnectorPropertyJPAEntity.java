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

package org.openengsb.persistence.connector.jpabackend;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.openengsb.core.api.persistence.PersistenceException;

@Entity(name = "CONNECTOR_PROPERTY")
public class ConnectorPropertyJPAEntity {

    @Column(name = "STRVALUE", nullable = false, length = 511)
    private String strValue;
    @Column(name = "CLASSNAME", nullable = false, length = 127)
    private String clazz;

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public static ConnectorPropertyJPAEntity getFromObject(Object obj) {
        ConnectorPropertyJPAEntity entity = new ConnectorPropertyJPAEntity();
        entity.setStrValue(obj.toString());
        entity.setClazz(ClassUtils.primitiveToWrapper(obj.getClass()).getName());
        return entity;
    }

    public Object toObject() throws PersistenceException {
        try {
            Class<?> clazz = Class.forName(this.clazz);
            return ConstructorUtils.invokeConstructor(clazz, this.strValue);
        } catch (Exception ex) {
            throw new PersistenceException(ex);
        }
    }
}
