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

package org.openengsb.core.workflow.model;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;

public class RuleBaseElement implements Serializable {

    private static final long serialVersionUID = -4904033275614618650L;

    private String code;
    private RuleBaseElementType type;
    private String packageName;
    private String name;

    public RuleBaseElement() {
    }

    public RuleBaseElement(RuleBaseElementId id) {
        name = id.getName();
        packageName = id.getPackageName();
        type = id.getType();
    }

    public RuleBaseElement(RuleBaseElementId id, String code) {
        this(id);
        this.code = code;
    }

    public RuleBaseElementType getType() {
        return type;
    }

    public void setType(RuleBaseElementType type) {
        this.type = type;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public RuleBaseElementId generateId() {
        return new RuleBaseElementId(type, packageName, name);
    }

    @Override
    public String toString() {
        return String.format("%s %s[%s]", type, packageName, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RuleBaseElement)) {
            return false;
        }
        RuleBaseElement other = (RuleBaseElement) obj;
        return (code == null || code.equals(other.code)) && (type == null || type.equals(other.type))
                && (packageName == null || packageName.equals(other.packageName))
                && (name == null || name.equals(other.name));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(code).append(type).append(packageName).append(name).toHashCode();
    }
}
