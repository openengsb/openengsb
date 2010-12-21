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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.arg == null) ? 0 : this.arg.hashCode());
        result = prime * result + ((this.metaData == null) ? 0 : this.metaData.hashCode());
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
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
        if (this.arg == null) {
            if (other.arg != null) {
                return false;
            }
        } else if (!this.arg.equals(other.arg)) {
            return false;
        }
        if (this.metaData == null) {
            if (other.metaData != null) {
                return false;
            }
        } else if (!this.metaData.equals(other.metaData)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", type, arg);
    }

    public String getClassName() {
        if (this.arg != null) {
            return this.arg.getClass().getName();
        } else {
            return "";
        }
    }
}
