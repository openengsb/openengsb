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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Representation of a most general method call containing a {@link #methodName}, {@link #args} you want to give to the
 * method and so called {@link #metaData}. Since the target system often requires additional information for calling
 * specific methods (e.g. context setup, target thread, security, active user, ...) it is allowed to add additional
 * information to each method call to make. Finally this abstraction can extract all {@link Class} objects in the
 * {@link #getClasses()} required to load this method call correctly into the class loader. The classes are used to
 * identify the right method.
 */
@SuppressWarnings("serial")
@XmlRootElement
public class MethodCall implements Serializable {

    private String methodName;
    private Object[] args;
    private Map<String, String> metaData;
    private List<String> classes;

    public MethodCall() {
    }

    public MethodCall(String methodName, Object[] args) {
        this(methodName, args, new HashMap<String, String>());
    }

    public MethodCall(String methodName, Object[] args, List<String> classes) {
        this(methodName, args, new HashMap<String, String>(), classes);
    }

    public MethodCall(String methodName, Object[] args, Map<String, String> metaData, List<String> classes) {
        super();
        this.methodName = methodName;
        this.args = args;
        this.metaData = metaData;
        this.classes = classes;
    }

    public MethodCall(String methodName, Object[] args, Map<String, String> metaData) {
        this(methodName, args, metaData, null);
        classes = getRealClassImplementation();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
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
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(args);
        result = prime * result + ((classes == null) ? 0 : classes.hashCode());
        result = prime * result + ((metaData == null) ? 0 : metaData.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
        MethodCall other = (MethodCall) obj;
        if (!Arrays.equals(args, other.args)) {
            return false;
        }
        if (classes == null) {
            if (other.classes != null) {
                return false;
            }
        } else if (!classes.equals(other.classes)) {
            return false;
        }
        if (metaData == null) {
            if (other.metaData != null) {
                return false;
            }
        } else if (!metaData.equals(other.metaData)) {
            return false;
        }
        if (methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        } else if (!methodName.equals(other.methodName)) {
            return false;
        }
        return true;
    }

}
