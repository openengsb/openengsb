/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.core;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class ReturnValueTransformerTest {

    @Test
    public void testVoid() throws SerializationException {
        ReturnValue input = new ReturnValue(null, void.class);

        String xml = Transformer.toXml(input);
        ReturnValue output = Transformer.toReturnValue(xml);

        check(input, output);
    }

    @Test
    public void testPrimitive() throws SerializationException {
        ReturnValue input = new ReturnValue("success", String.class);

        String xml = Transformer.toXml(input);
        ReturnValue output = Transformer.toReturnValue(xml);

        check(input, output);
    }

    @Test
    public void testBean() throws SerializationException {
        TestBean testBean = new TestBean("foo", 42, null);
        ReturnValue input = new ReturnValue(testBean, TestBean.class);

        String xml = Transformer.toXml(input);
        ReturnValue output = Transformer.toReturnValue(xml);

        check(input, output);
    }

    @Test
    public void testBeanReference() throws SerializationException {
        TestBean testBeanA = new TestBean("foo", 42, null);
        TestBean testBeanB = new TestBean("bar", 44, testBeanA);
        testBeanA.setBean(testBeanB);
        ReturnValue input = new ReturnValue(testBeanB, TestBean.class);

        String xml = Transformer.toXml(input);
        ReturnValue output = Transformer.toReturnValue(xml);

        check(input, output);

        TestBean beanB = ((TestBean) output.getValue());
        TestBean beanA = beanB.getBean();

        Assert.assertTrue(beanB == beanA.getBean());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testList() throws SerializationException {
        List<String> inList = Arrays.asList("1", "2", "3");

        ReturnValue input = new ReturnValue(inList, inList.getClass());

        String xml = Transformer.toXml(input);
        ReturnValue output = Transformer.toReturnValue(xml);

        List<String> outList = (List<String>) output.getValue();

        Assert.assertEquals(input.getType(), output.getType());
        Assert.assertEquals(inList.size(), outList.size());

        for (int i = 0; i < inList.size(); i++) {
            Assert.assertEquals(inList.get(i), outList.get(i));
        }
    }

    private void check(ReturnValue expected, ReturnValue actual) {
        Assert.assertEquals(expected.getType(), actual.getType());
        Assert.assertEquals(expected.getValue(), actual.getValue());
    }

    public static class TestBean {
        private String string;
        private int i;
        private TestBean bean;

        public TestBean() {
        }

        public TestBean(String string, int i, TestBean bean) {
            this.string = string;
            this.i = i;
            this.bean = bean;
        }

        public TestBean getBean() {
            return bean;
        }

        public void setBean(TestBean bean) {
            this.bean = bean;
        }

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + i;
            result = prime * result + ((string == null) ? 0 : string.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestBean other = (TestBean) obj;
            if (i != other.i)
                return false;
            if (string == null) {
                if (other.string != null)
                    return false;
            } else if (!string.equals(other.string))
                return false;
            return true;
        }
    }

    public static class TestBeanArray {
        private int[] intArray;

        private TestBean[] testBeanArray;

        public TestBeanArray() {
        }

        public void addTestData() {
            intArray = new int[] { 42, 1, 2, 3 };
            TestBean testBeanA = new TestBean("foo", 4, null);
            testBeanArray = new TestBean[] { testBeanA, new TestBean("bar", 5, testBeanA) };
        }

        public int[] getIntArray() {
            return intArray;
        }

        public TestBean[] getTestBeanArray() {
            return testBeanArray;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(intArray);
            result = prime * result + Arrays.hashCode(testBeanArray);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestBeanArray other = (TestBeanArray) obj;
            if (!Arrays.equals(intArray, other.intArray))
                return false;
            if (!Arrays.equals(testBeanArray, other.testBeanArray))
                return false;
            return true;
        }

    }
}
