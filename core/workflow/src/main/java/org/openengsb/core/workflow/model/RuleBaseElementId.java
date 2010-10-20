/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@SuppressWarnings("serial")
@XmlType(propOrder = { "type", "packageName", "name" })
public class RuleBaseElementId implements Serializable {

    public static final String DEFAULT_RULE_PACKAGE = "org.openengsb";

    private RuleBaseElementType type;
    private String packageName;
    private String name;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.name == null ? 0 : this.name.hashCode());
        result = prime * result + (this.packageName == null ? 0 : this.packageName.hashCode());
        result = prime * result + (this.type == null ? 0 : this.type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RuleBaseElementId other = (RuleBaseElementId) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.packageName == null) {
            if (other.packageName != null) {
                return false;
            }
        } else if (!this.packageName.equals(other.packageName)) {
            return false;
        }
        if (this.type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!this.type.equals(other.type)) {
            return false;
        }
        return true;
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
