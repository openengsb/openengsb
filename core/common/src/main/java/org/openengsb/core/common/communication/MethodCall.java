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

package org.openengsb.core.common.communication;

import java.util.HashMap;
import java.util.Map;

public class MethodCall {

    private String methodName;
    private Object[] args;
    private Map<String, String> metaData;

    public MethodCall(String methodName, Object[] args, Map<String, String> metaData) {
        this.methodName = methodName;
        this.args = args;
        this.metaData = metaData;
    }

    public MethodCall(String methodName, Object[] args) {
        this(methodName, args, new HashMap<String, String>());
    }

    public MethodCall() {
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Map<String, String> getMetaData() {
        return this.metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

}
