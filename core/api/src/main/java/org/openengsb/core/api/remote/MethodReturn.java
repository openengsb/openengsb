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

import java.util.List;
import java.util.Map;

/**
 * This object wraps the return values of a remote method call. The different types which could be returned are stored
 * in {@link #type}. The object itself is available via {@link #getArg()}. Since this is the result of an remote call it
 * is possible that additional meta-data was added (describing e.g. context, username, ...) could have been added to
 * this message. Those could be retrieved via {@link #getMetaData()}.
 */
public class MethodReturn {
    public enum ReturnType {
            Void, Object, Exception,
    }

    private ReturnType type;
    private Object arg;
    private Map<String, String> metaData;
    private String callId;
    private String className;

    public MethodReturn(ReturnType type, Object arg, Map<String, String> metaData, String callId) {
        this.type = type;
        this.metaData = metaData;
        this.callId = callId;
        setArg(arg);
    }

    public MethodReturn() {
    }

    public ReturnType getType() {
        return type;
    }

    public void setType(ReturnType type) {
        this.type = type;
    }

    public Object getArg() {
        return arg;
    }

    public void setArg(Object arg) {
        this.arg = arg;
        if (arg != null) {
            if (arg instanceof List<?>) {
                className = List.class.getName();
            } else {
                className = arg.getClass().getName();
            }
        } else {
            className = "";
        }
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (arg == null ? 0 : arg.hashCode());
        result = prime * result + (metaData == null ? 0 : metaData.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
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
        MethodReturn other = (MethodReturn) obj;
        if (arg == null) {
            if (other.arg != null) {
                return false;
            }
        } else if (!arg.equals(other.arg)) {
            return false;
        }
        if (metaData == null) {
            if (other.metaData != null) {
                return false;
            }
        } else if (!metaData.equals(other.metaData)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", type, arg);
    }

}
