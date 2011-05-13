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

import java.util.HashMap;
import java.util.Map;

public class MethodResult {

    public enum ReturnType {
            Void, Object, Exception,
    }

    private String className;
    private Object arg;
    private ReturnType type;
    private Map<String, String> metaData;

    public MethodResult() {
    }

    public MethodResult(Object arg) {
        this(arg, ReturnType.Object);
    }

    public MethodResult(Object arg, ReturnType type) {
        this(arg, type, arg.getClass().getName());
    }

    public MethodResult(Object arg, ReturnType type, String className) {
        this(arg, type, className, new HashMap<String, String>());
    }

    public MethodResult(Object arg, ReturnType type, String className, Map<String, String> metaData) {
        this.className = className;
        this.arg = arg;
        this.type = type;
        this.metaData = metaData;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object getArg() {
        return arg;
    }

    public void setArg(Object arg) {
        this.arg = arg;
    }

    public ReturnType getType() {
        return type;
    }

    public void setType(ReturnType type) {
        this.type = type;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", arg, type);
    }

    public static MethodResult newVoidResult() {
        return new MethodResult(null, ReturnType.Void, void.class.getName());
    }
}
