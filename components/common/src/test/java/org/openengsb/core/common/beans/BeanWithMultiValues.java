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

import com.google.common.base.Objects;

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
        return Objects.hashCode(id, numbers, strings);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BeanWithMultiValues)) {
            return false;
        }
        BeanWithMultiValues other = (BeanWithMultiValues) o;
        return Objects.equal(id, id) && Objects.equal(numbers, other.numbers)
                && Objects.equal(strings, other.strings);
    }

}
