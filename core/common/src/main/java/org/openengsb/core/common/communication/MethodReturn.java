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

import java.util.Map;

public class MethodReturn {
    public enum ReturnType {
        Object, Exception,
    }

    private ReturnType type;
    private Object arg;
    private Map<String, String> metaData;

    public MethodReturn(ReturnType type, Object arg, Map<String, String> metaData) {
        this.type = type;
        this.arg = arg;
        this.metaData = metaData;
    }

    public MethodReturn() {
    }

    public ReturnType getType() {
        return this.type;
    }

    public void setType(ReturnType type) {
        this.type = type;
    }

    public Object getArg() {
        return this.arg;
    }

    public void setArg(Object arg) {
        this.arg = arg;
    }

    public Map<String, String> getMetaData() {
        return this.metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public String getClassName() {
        return this.arg.getClass().getName();
    }
}
