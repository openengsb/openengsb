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

package org.openengsb.core.api.workflow.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
@XmlType(propOrder = { "type", "packageName", "name" })
public class RuleBaseElementId implements Serializable {

    public static final String DEFAULT_RULE_PACKAGE = "org.openengsb";

    private RuleBaseElementType type;
    private String packageName;
    private String name;

    /**
     * package defaults to "org.openengsb"
     */
    public RuleBaseElementId(RuleBaseElementType type, String name) {
        this.type = type;
        this.name = name;
        this.packageName = DEFAULT_RULE_PACKAGE;
    }

    public RuleBaseElementId(RuleBaseElementType type, String packageName, String name) {
        this.type = type;
        this.packageName = packageName;
        this.name = name;
    }

    public RuleBaseElementId() {
        this.packageName = DEFAULT_RULE_PACKAGE;
    }

    @XmlElement(required = true)
    public RuleBaseElementType getType() {
        return this.type;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public String getName() {
        return this.name;
    }

    public void setType(RuleBaseElementType type) {
        this.type = type;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, packageName, type);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RuleBaseElementId)) {
            return false;
        }
        RuleBaseElementId other = (RuleBaseElementId) o;
        return Objects.equal(name, other.name) && Objects.equal(packageName, other.packageName)
                && Objects.equal(type, other.type);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(packageName);
        if (name != null) {
            result.append('.').append(name).toString();
        }
        return result.toString();
    }
}
