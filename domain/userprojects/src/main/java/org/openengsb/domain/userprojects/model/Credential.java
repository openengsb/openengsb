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

package org.openengsb.domain.userprojects.model;

import java.util.Objects;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.openengsb.labs.delegation.service.Provide;

/**
 * Note: To make sure that this object can be managed properly by the EDB it is recommended to call the generateUuid
 * method after setting all object attributes.
 */
@Provide(context = { Constants.DELEGATION_CONTEXT_MODELS })
@Model
public class Credential {

    @OpenEngSBModelId
    private String uuid;

    private String type;

    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean generateUuid(String owner) {

        if (type == null) {
            return false;
        } else {
            uuid = "Cred+" + owner + "+" + type;

            return true;
        }
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", type, value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Credential)) {
            return false;
        }
        final Credential other = (Credential) obj;
        return Objects.equals(type, other.type) && Objects.equals(value, other.value);
    }
}
