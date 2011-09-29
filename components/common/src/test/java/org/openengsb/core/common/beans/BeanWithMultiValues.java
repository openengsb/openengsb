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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class BeanWithMultiValues {

    private Long id;
    private List<String> strings;
    private List<Double> numbers;

    public BeanWithMultiValues() {
    }

    public BeanWithMultiValues(long id) {
        this.id = id;
    }

    public BeanWithMultiValues(long id, String... strings) {
        this.id = id;
        this.strings = Arrays.asList(strings);
    }

    public BeanWithMultiValues(long id, double... numbers) {
        this.id = id;
        this.numbers = Arrays.asList(ArrayUtils.toObject(numbers));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    public List<Double> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<Double> numbers) {
        this.numbers = numbers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((numbers == null) ? 0 : numbers.hashCode());
        result = prime * result + ((strings == null) ? 0 : strings.hashCode());
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
        BeanWithMultiValues other = (BeanWithMultiValues) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (numbers == null) {
            if (other.numbers != null) {
                return false;
            }
        } else if (!numbers.equals(other.numbers)) {
            return false;
        }
        if (strings == null) {
            if (other.strings != null) {
                return false;
            }
        } else if (!strings.equals(other.strings)) {
            return false;
        }
        return true;
    }

}
