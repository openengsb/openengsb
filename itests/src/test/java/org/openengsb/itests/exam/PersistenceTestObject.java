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

package org.openengsb.itests.exam;

import java.io.Serializable;

import com.google.common.base.Objects;

public class PersistenceTestObject implements Serializable {

    private static final long serialVersionUID = 490651433247221771L;

    private String string;
    private final Integer integer;

    public PersistenceTestObject(String string, Integer integer) {
        this.string = string;
        this.integer = integer;
    }

    public Integer getInteger() {
        return integer;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(integer, string);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PersistenceTestObject)) {
            return false;
        }
        PersistenceTestObject other = (PersistenceTestObject) o;
        return Objects.equal(integer, other.integer) && Objects.equal(string, other.string);
    }

}
