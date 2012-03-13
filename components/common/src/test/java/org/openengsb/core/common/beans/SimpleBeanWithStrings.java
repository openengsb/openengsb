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

public class SimpleBeanWithStrings {
    private String value1;
    private String value2;

    public SimpleBeanWithStrings() {
    }

    public SimpleBeanWithStrings(String value1, String value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value1, value2);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimpleBeanWithStrings)) {
            return false;
        }
        SimpleBeanWithStrings other = (SimpleBeanWithStrings) o;
        return Objects.equal(value1, other.value1) && Objects.equal(value2, other.value2);
    }

}
