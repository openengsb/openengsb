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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.base.Objects;

/**
 * Representation of a most general method call containing a {@link #methodName}, {@link #args} you want to give to the
 * method and so called {@link #metaData}. Since the target system often requires additional information for calling
 * specific methods (e.g. context setup, target thread, security, active user, ...) it is allowed to add additional
 * information to each method call to make. Finally this abstraction can extract all {@link Class} objects in the
 * {@link #getClasses()} required to load this method call correctly into the class loader. The classes are used to
 * identify the right method.
 */
@SuppressWarnings("serial")
public class MethodCall implements Serializable {

    private String methodName;
    private Object[] args;
    private Map<String, String> metaData;

    public MethodCall() {
    }

    public MethodCall(String methodName, Object[] args) {
        this(methodName, args, new HashMap<String, String>());
    }

    public MethodCall(String methodName, Object[] args, Map<String, String> metaData) {
        super();
        this.methodName = methodName;
        this.args = args;
        this.metaData = metaData;
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

    public List<String> doGetRealClassImplementation() {
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
        return Objects.hashCode(methodName, args, metaData);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodCall) {
            MethodCall other = (MethodCall) obj;
            return Objects.equal(methodName, other.methodName)
                    && Objects.equal(args, other.args)
                    && Objects.equal(metaData, other.metaData);
        }
        return false;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("methodName", methodName)
            .add("args", ArrayUtils.toString(args))
            .add("metadata", metaData).toString();
    }

}
