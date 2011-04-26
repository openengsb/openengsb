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

package org.openengsb.core.api.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of a most general method call containing a {@link #methodName}, {@link #args} you want to give to the
 * method and so called {@link #metaData}. Since the target system often requires additional information for calling
 * specific methods (e.g. context setup, target thread, security, active user, ...) it is allowed to add additional
 * information to each method call to make. Finally this abstraction can extract all {@link Class} objects in the
 * {@link #getClasses()} required to load this method call correctly into the class loader. The classes are used to
 * identify the right method.
 */
public class MethodCall {

    private String methodName;
    private Object[] args;
    private Map<String, String> metaData;
    private String callId;
    private boolean answer;
    private List<String> classes;

    public MethodCall() {
        metaData = new HashMap<String, String>();
    }

    public MethodCall(String methodName, Object[] args, Map<String, String> metaData, String callId, boolean answer,
            List<String> classes) {
        this.methodName = methodName;
        this.args = args;
        this.metaData = metaData;
        this.callId = callId;
        this.answer = answer;
        this.classes = classes;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public boolean isAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        if (args == null) {
            return new Object[0];
        }
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }


    public List<String> getClasses() {
        if (classes == null) {
            return getRealClassImplementation();
        }
        return classes;
    }

    public List<String> getRealClassImplementation() {
        List<String> argsClasses = new ArrayList<String>();
        if (getArgs() != null) {
            for (Object object : getArgs()) {
                if (object instanceof List<?>) {
                    argsClasses.add(List.class.getName());
                } else {
                    argsClasses.add(object.getClass().getName());
                }
            }
        }
        return argsClasses;
    }
}
