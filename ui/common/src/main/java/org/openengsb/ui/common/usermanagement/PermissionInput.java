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
package org.openengsb.ui.common.usermanagement;

import java.io.Serializable;
import java.util.Map;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.util.BeanUtilsExtended;

public class PermissionInput implements Serializable {
    private static final long serialVersionUID = 1257288811611665273L;

    enum State {
        UNMODIFIED, NEW, UPDATED, DELETED
    }

    private Class<?> type;

    private Map<String, String> values;

    private State state = State.UNMODIFIED;

    public PermissionInput(Class<?> type, Map<String, String> values) {
        this.values = values;
        this.type = type;
    }

    public PermissionInput(Class<?> type, Map<String, String> values, State state) {
        this.type = type;
        this.values = values;
        this.state = state;
    }

    public PermissionInput() {
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Permission toPermission() {
        return (Permission) BeanUtilsExtended.buildBeanFromAttributeMap(type, values);
    }

    @Override
    public String toString() {
        return toPermission().toString();
    }

}
