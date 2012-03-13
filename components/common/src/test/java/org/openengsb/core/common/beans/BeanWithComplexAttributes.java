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

import java.math.BigDecimal;

import com.google.common.base.Objects;

public class BeanWithComplexAttributes {

    private CustomStringClass value;
    private BigDecimal number;

    public BeanWithComplexAttributes() {
    }

    public BeanWithComplexAttributes(BigDecimal number) {

        this.number = number;
    }

    public BeanWithComplexAttributes(CustomStringClass value) {
        this.value = value;
    }

    public BeanWithComplexAttributes(CustomStringClass value, BigDecimal number) {
        this.value = value;
        this.number = number;
    }

    public CustomStringClass getValue() {
        return value;
    }

    public void setValue(CustomStringClass value) {
        this.value = value;
    }

    public BigDecimal getNumber() {
        return number;
    }

    public void setNumber(BigDecimal number) {
        this.number = number;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(number, value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BeanWithComplexAttributes)) {
            return false;
        }
        BeanWithComplexAttributes other = (BeanWithComplexAttributes) o;
        return Objects.equal(number, other.number) && Objects.equal(value, other.value);
    }

}
