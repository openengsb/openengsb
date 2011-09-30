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

package org.openengsb.core.common;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openengsb.core.common.beans.BeanWithComplexAttributes;
import org.openengsb.core.common.beans.BeanWithFaultyGetter;
import org.openengsb.core.common.beans.BeanWithMultiValues;
import org.openengsb.core.common.beans.BeanWithProtectedProperties;
import org.openengsb.core.common.beans.CustomStringClass;
import org.openengsb.core.common.beans.SimpleBeanWithStrings;
import org.openengsb.core.common.util.BeanUtilsExtended;

public class BeanUtilsExtendedTest {

    @Test
    public void buildAttributeMapFromSimpleStringBean_shouldReturnMapThatContainsProperties() throws Exception {
        SimpleBeanWithStrings testBean = new SimpleBeanWithStrings("foo", "bar");
        Map<String, String> attributeMap = BeanUtilsExtended.buildStringAttributeMap(testBean);
        assertThat(attributeMap.get("value1"), is("foo"));
        assertThat(attributeMap.get("value2"), is("bar"));
    }

    @Test
    public void buildAttributeMapAndBuildNewBean_shouldBeEqualToOriginalBean() throws Exception {
        SimpleBeanWithStrings testBean = new SimpleBeanWithStrings("foo", "bar");
        Map<String, String> attributeMap = BeanUtilsExtended.buildStringAttributeMap(testBean);
        SimpleBeanWithStrings bean2 =
            BeanUtilsExtended.createBeanFromAttributeMap(SimpleBeanWithStrings.class, attributeMap);
        assertThat(bean2, is(testBean));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void buildMapFromMultiValueBean_shouldContainAllValues() throws Exception {
        BeanWithMultiValues testBean = new BeanWithMultiValues(42, 2.0, 3.1415, 1.4142135);
        Map<String, Object> map = BeanUtilsExtended.buildObjectAttributeMap(testBean);
        assertThat((Long) map.get("id"), is(42L));
        assertThat((List<Double>) map.get("numbers"), is(Arrays.asList(2.0, 3.1415, 1.4142135)));
    }

    @Test
    public void buildMapFromMultiValueBeanAndRebuild_shouldBeEqualtoOriginalBean() throws Exception {
        BeanWithMultiValues testBean = new BeanWithMultiValues(42, 2.0, 3.1415, 1.4142135);
        Map<String, Object> map = BeanUtilsExtended.buildObjectAttributeMap(testBean);
        BeanWithMultiValues built = BeanUtilsExtended.createBeanFromAttributeMap(BeanWithMultiValues.class, map);
        assertThat(built, is(testBean));
    }

    @Test
    public void buildMapWithComlexBeanAndRebuild_shouldBeEqualToOriginalBean() throws Exception {
        BeanWithComplexAttributes bean =
            new BeanWithComplexAttributes(new CustomStringClass("foo:bar"), new BigDecimal("1"));
        Map<String, Object> map = BeanUtilsExtended.buildObjectAttributeMap(bean);
        BeanWithComplexAttributes created =
            BeanUtilsExtended.createBeanFromAttributeMap(BeanWithComplexAttributes.class, map);
        assertThat(created, is(bean));
    }

    @Test
    public void buildMapWithIncompleteComlexBeanAndRebuild_shouldBeEqualToOriginalBean() throws Exception {
        BeanWithComplexAttributes bean =
            new BeanWithComplexAttributes(new CustomStringClass("foo:bar"));
        Map<String, Object> map = BeanUtilsExtended.buildObjectAttributeMap(bean);
        BeanWithComplexAttributes created =
            BeanUtilsExtended.createBeanFromAttributeMap(BeanWithComplexAttributes.class, map);
        assertThat(created, is(bean));
    }

    @Test
    public void buildMapWithBeanWithProtectedPropertiesAndRebuild_shouldBeEqualToOriginalBean() throws Exception {
        BeanWithProtectedProperties bean =
            new BeanWithProtectedProperties("foo", "bar");
        Map<String, Object> map = BeanUtilsExtended.buildObjectAttributeMap(bean);
        BeanWithProtectedProperties created =
            BeanUtilsExtended.createBeanFromAttributeMap(BeanWithProtectedProperties.class, map);
        assertThat(created.getPublicValue(), is("foo"));
        assertThat(created, not(is(bean)));
    }

    @Test(expected = SecurityException.class)
    public void buildMapFromBeanWithFaultyGetter_shouldThrowException() throws Exception {
        BeanWithFaultyGetter bean = new BeanWithFaultyGetter();
        BeanUtilsExtended.buildObjectAttributeMap(bean);
    }

}
