/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.methodcalltransformation.ReturnValue;
import org.openengsb.core.methodcalltransformation.ReturnValueTransformer;

public class TestReturnValueTransformer {

    @Test
    public void testPrimitive() {
        ReturnValue input = new ReturnValue("success", String.class);

        Segment intermediate = ReturnValueTransformer.transform(input);
        ReturnValue output = ReturnValueTransformer.transform(intermediate);

        check(input, output);
    }

    @Test
    public void testBean() {
        TestBean testBean = new TestBean("foo", 42, null);
        ReturnValue input = new ReturnValue(testBean, TestBean.class);

        Segment intermediate = ReturnValueTransformer.transform(input);
        ReturnValue output = ReturnValueTransformer.transform(intermediate);

        check(input, output);
    }

    @Test
    public void testBeanReference() {
        TestBean testBeanA = new TestBean("foo", 42, null);
        TestBean testBeanB = new TestBean("bar", 44, testBeanA);
        testBeanA.setBean(testBeanB);
        ReturnValue input = new ReturnValue(testBeanB, TestBean.class);

        Segment intermediate = ReturnValueTransformer.transform(input);
        ReturnValue output = ReturnValueTransformer.transform(intermediate);

        check(input, output);

        TestBean beanB = ((TestBean) output.getValue());
        TestBean beanA = beanB.getBean();

        Assert.assertTrue(beanB == beanA.getBean());
    }

    @Test
    public void testArray() {
        String[] inArray = new String[] { "1", "2", "3" };

        ReturnValue input = new ReturnValue(inArray, inArray.getClass());

        Segment intermediate = ReturnValueTransformer.transform(input);
        ReturnValue output = ReturnValueTransformer.transform(intermediate);

        String[] outArray = (String[]) output.getValue();

        Assert.assertEquals(input.getType(), output.getType());
        Assert.assertEquals(inArray.length, outArray.length);

        for (int i = 0; i < outArray.length; i++) {
            Assert.assertEquals(inArray[i], outArray[i]);
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
