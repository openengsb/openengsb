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

import java.util.List;
import java.util.Objects;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.openengsb.labs.delegation.service.Provide;

import com.google.common.collect.Lists;

/**
 * Note: To make sure that this object can be managed properly by the EDB it is recommended to call the generateUuid
 * method after setting all object attributes.
 */
@Provide(context = { Constants.DELEGATION_CONTEXT_MODELS })
@Model
public class Attribute {

    @OpenEngSBModelId
    private String uuid;

    private String attributeName;

    private List<Object> values = Lists.newArrayList();

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public void generateUuid(String owner) {
        uuid = "Attr+" + owner + "+" + attributeName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return String.format("%s", attributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeName, values);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Attribute)) {
            return false;
        }
        final Attribute other = (Attribute) obj;
        return Objects.equals(attributeName, other.attributeName) && Objects.equals(values, other.values);
    }
}
