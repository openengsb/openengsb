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

import java.util.HashMap;
import java.util.Map;

public class GlobalDeclaration {

    private static final String META_GLOBAL = "global";
    private static final String META_GLOBAL_VARIABLE = "variable";

    private String className;
    private String variableName;

    public GlobalDeclaration() {
    }

    public GlobalDeclaration(String variableName) {
        this.variableName = variableName;
    }

    public GlobalDeclaration(String className, String variableName) {
        this.className = className;
        this.variableName = variableName;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getVariableName() {
        return this.variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public Map<String, String> toMetadata() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put(META_GLOBAL, META_GLOBAL);
        if (this.getVariableName() != null) {
            ret.put(META_GLOBAL_VARIABLE, this.getVariableName());
        }
        return ret;
    }

}
