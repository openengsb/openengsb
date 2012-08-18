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

package org.openengsb.ui.admin.xlink.mocking;

import org.openengsb.core.api.model.annotation.Model;

/**
 * Example modelObject for an ObjectOriented-Sourcecode Domain
 */
@Model
public class ExampleObjectOrientedModel {

    private String ooMethodName;
    
    private String ooClassName;
    
    private String ooPackageName;

    public String getOoClassName() {
        return ooClassName;
    }

    public void setOoClassName(String ooClassName) {
        this.ooClassName = ooClassName;
    }

    public String getOoMethodName() {
        return ooMethodName;
    }

    public void setOoMethodName(String ooMethodName) {
        this.ooMethodName = ooMethodName;
    }

    public String getOoPackageName() {
        return ooPackageName;
    }

    public void setOoPackageName(String ooPackageName) {
        this.ooPackageName = ooPackageName;
    }

}
