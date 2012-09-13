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

package org.openengsb.core.util.beans;

public class BeanWithProtectedProperties {

    private String publicValue;
    private String privateValue;

    public BeanWithProtectedProperties() {
    }

    public BeanWithProtectedProperties(String publicValue, String privateValue) {
        this.publicValue = publicValue;
        this.privateValue = privateValue;
    }

    public String getPublicValue() {
        return publicValue;
    }

    public void setPublicValue(String publicValue) {
        this.publicValue = publicValue;
    }

    protected String getPrivateValue() {
        return privateValue;
    }

    protected void setPrivateValue(String privateValue) {
        this.privateValue = privateValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((privateValue == null) ? 0 : privateValue.hashCode());
        result = prime * result + ((publicValue == null) ? 0 : publicValue.hashCode());
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
        BeanWithProtectedProperties other = (BeanWithProtectedProperties) obj;
        if (privateValue == null) {
            if (other.privateValue != null) {
                return false;
            }
        } else if (!privateValue.equals(other.privateValue)) {
            return false;
        }
        if (publicValue == null) {
            if (other.publicValue != null) {
                return false;
            }
        } else if (!publicValue.equals(other.publicValue)) {
            return false;
        }
        return true;
    }

}
