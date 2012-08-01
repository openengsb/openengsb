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

package org.openengsb.core.ekb.impl.internal.models;

import java.util.List;

public class ModelB {
    private String idB;
    private String testB;
    private String blubB;
    private List<String> elements;
    private Integer intValue;

    public String getIdB() {
        return idB;
    }

    public void setIdB(String idB) {
        this.idB = idB;
    }

    public String getTestB() {
        return testB;
    }

    public void setTestB(String testB) {
        this.testB = testB;
    }

    public String getBlubB() {
        return blubB;
    }

    public void setBlubB(String blubB) {
        this.blubB = blubB;
    }
    
    public List<String> getElements() {
        return this.elements;
    }
    
    public void setElements(List<String> elements) {
        this.elements = elements;
    }
    
    public Integer getIntValue() {
        return this.intValue;
    }
    
    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }
}
