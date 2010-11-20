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

package org.openengsb.core.workflow.internal.persistence;

import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;

public class RuleBaseElement {
    private String code;
    private RuleBaseElementType type;
    private String packageName;
    private String name;

    public RuleBaseElement() {
    }

    public RuleBaseElement(RuleBaseElementId id) {
        this.name = id.getName();
        this.packageName = id.getPackageName();
        this.type = id.getType();
    }

    public RuleBaseElement(RuleBaseElementId id, String code) {
        this(id);
        this.code = code;
    }

    public RuleBaseElementType getType() {
        return this.type;
    }

    public void setType(RuleBaseElementType type) {
        this.type = type;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public RuleBaseElementId getId() {
        return new RuleBaseElementId(type, packageName, name);
    }

    @Override
    public String toString() {
        return String.format("%s %s[%s]", type, packageName, name);
    }

}
