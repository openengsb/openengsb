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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public MethodCall(Method method, Object[] args) {
        this(method, args, Collections.<String, String>emptyMap());
    }
    
    public MethodCall(Method method, Object[] args, Map<String, String> metaData) {
        this(method.getName(), args, metaData, getRealClassImplementation(method, args));
    }
    
    public MethodCall(String methodName, Object[] args) {
        this(methodName, args, Collections.<String, String>emptyMap());
    }
    
    public MethodCall(String methodName, Object[] args, Map<String, String> metaData) {
        this(methodName, args, metaData, getRealClassImplementation(null, args));
    }
    
    public MethodCall(String methodName, Object[] args, List<String> classes) {
        this(methodName, args, Collections.<String, String>emptyMap(), classes);
    }

    public MethodCall(String methodName, Object[] args, Map<String, String> metaData, List<String> classes) {
        super();
        this.methodName = methodName;
        this.args = args;
        this.classes = classes;
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

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    private static List<String> getRealClassImplementation(Method method, Object[] args) {
        if (args == null) {
            return Collections.emptyList();
        }
        List<String> argClasses = new ArrayList<String>(args.length);
        for (int i = 0; i < args.length; i++) {
            argClasses.add(getArgumentClass(method, args[i], i));
        }
        return argClasses;
    }
    
    private static String getArgumentClass(Method method, Object arg, int argIndex) {
        if (arg == null) {
            if (method == null) {
                throw new IllegalArgumentException("cannot determine type of null argument");
            }
            return method.getParameterTypes()[argIndex].getName();
        } else if (arg instanceof List<?>) {
            return List.class.getName();
        } else {
            return arg.getClass().getName();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodName, args, classes, metaData);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MethodCall) {
            MethodCall other = (MethodCall) obj;
            return Objects.equals(methodName, other.methodName)
                    && Objects.deepEquals(args, other.args)
                    && Objects.equals(classes, other.classes)
                    && Objects.equals(metaData, other.metaData);
        }
        return false;
    }

}
