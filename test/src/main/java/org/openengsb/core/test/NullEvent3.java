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

package org.openengsb.core.test;

import org.openengsb.core.api.model.annotation.Model;

@Model
public class NullEvent3 extends NullEvent2 {

    private String testStringProp;

    private int testIntProp;

    private boolean testBoolProp;

    public String getTestStringProp() {
        return testStringProp;
    }

    public void setTestStringProp(String testStringProp) {
        this.testStringProp = testStringProp;
    }

    public int getTestIntProp() {
        return testIntProp;
    }

    public void setTestIntProp(int testIntProp) {
        this.testIntProp = testIntProp;
    }

    public boolean isTestBoolProp() {
        return testBoolProp;
    }

    public void setTestBoolProp(boolean testBoolProp) {
        this.testBoolProp = testBoolProp;
    }

}
