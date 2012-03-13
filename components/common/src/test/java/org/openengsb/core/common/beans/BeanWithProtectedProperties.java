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

package org.openengsb.core.common.beans;

import com.google.common.base.Objects;

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
        return Objects.hashCode(privateValue, publicValue);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BeanWithProtectedProperties)) {
            return false;
        }
        BeanWithProtectedProperties other = (BeanWithProtectedProperties) o;
        return Objects.equal(privateValue, other.privateValue)
                && Objects.equal(publicValue, other.publicValue);
    }

}
