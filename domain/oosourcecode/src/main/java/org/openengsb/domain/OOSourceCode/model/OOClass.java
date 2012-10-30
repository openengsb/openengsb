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

package org.openengsb.domain.OOSourceCode.model;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.labs.delegation.service.Provide;

@Provide(context = { Constants.DELEGATION_CONTEXT_MODELS })
@Model
public class OOClass {
    
    private String className;
    
    private String packageName;
    
    private OOVariable[] attributes; 
    
    private StringBuffer methods;

    public OOClass() { }
    
    public OOClass(String className, String packageName, OOVariable[] attributes, StringBuffer methods) {
        this.className = className;
        this.packageName = packageName;
        this.attributes = attributes;
        this.methods = methods;
    }
    
    public OOVariable[] getAttributes() {
        return attributes;
    }

    public void setAttributes(OOVariable[] attributes) {
        this.attributes = attributes;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public StringBuffer getMethods() {
        return methods;
    }

    public void setMethods(StringBuffer methods) {
        this.methods = methods;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

}
