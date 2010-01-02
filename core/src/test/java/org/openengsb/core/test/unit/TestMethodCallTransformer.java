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

package org.openengsb.core.test.unit;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class TestMethodCallTransformer {

    @Test
    public void testPrimitive() {
        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, "hallo" }, new Class<?>[] { int.class,
                long.class, String.class });

        Segment intermediate = Transformer.toSegment(input);
        MethodCall output = Transformer.toMethodCall(intermediate);

        check(input, output);
    }

    @Test
    public void testBean() throws SerializationException {
        TestBean beanA = new TestBean("testStringA", 42, null);
        TestBean beanB = new TestBean("testStringB", 3, beanA);
        beanA.setBean(beanB);

        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, beanA }, new Class<?>[] { int.class,
                long.class, TestBean.class });

        Segment intermediate = Transformer.toSegment(input);
        MethodCall output = Transformer.toMethodCall(intermediate);

        check(input, output);

        TestBean tbA = (TestBean) output.getArgs()[2];
        TestBean tbB = tbA.getBean();

        Assert.assertTrue(tbA == tbB.getBean());
    }

    @Test
    public void testSelfReferencingBean() throws SerializationException {
        TestBean beanA = new TestBean("bar", 42, null);
        beanA.setBean(beanA);

        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, beanA }, new Class<?>[] { int.class,
                long.class, TestBean.class });

        Segment intermediate = Transformer.toSegment(input);
        MethodCall output = Transformer.toMethodCall(intermediate);

        check(input, output);

        TestBean tbA = (TestBean) output.getArgs()[2];
        TestBean tbB = tbA.getBean();

        Assert.assertTrue(tbA == tbB.getBean());
    }

    @Test
    public void testBeanWithArray() throws Exception {
        TestBeanArray testBean = new TestBeanArray();
        testBean.addTestData();

        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, testBean }, new Class<?>[] { int.class,
                long.class, TestBeanArray.class });

        Segment intermediate = Transformer.toSegment(input);
        MethodCall output = Transformer.toMethodCall(intermediate);

        check(input, output);

        TestBeanArray tbArray = (TestBeanArray) output.getArgs()[2];
        TestBean tbA = tbArray.getTestBeanArray()[0];
        TestBean tbB = tbArray.getTestBeanArray()[1];

        Assert.assertTrue(tbA == tbB.getBean());
    }

    @Test
    public void testPrimitiveClasses() throws Exception {
        TestBeanArray testBean = new TestBeanArray();
        testBean.addTestData();

        MethodCall input = new MethodCall("foo", new Object[] { new Integer(42), new Byte("42"), new Short((short) 42),
                new Long(42L), new Character('4'), new Float(42.42), new Double(42.42), new Boolean(true) },
                new Class<?>[] { Integer.class, Byte.class, Short.class, Long.class, Character.class, Float.class,
                        Double.class, Boolean.class });

        Segment intermediate = Transformer.toSegment(input);
        MethodCall output = Transformer.toMethodCall(intermediate);

        check(input, output);
    }

    private void check(MethodCall expected, MethodCall actual) {
        Assert.assertEquals(expected.getMethodName(), actual.getMethodName());
        Assert.assertEquals(expected.getArgs().length, actual.getArgs().length);
        Assert.assertEquals(expected.getTypes().length, actual.getTypes().length);
        Assert.assertEquals(actual.getArgs().length, actual.getTypes().length);

        for (int i = 0; i < expected.getArgs().length; i++) {
            Assert.assertEquals(expected.getTypes()[i], actual.getTypes()[i]);
            Assert.assertEquals(expected.getArgs()[i], actual.getArgs()[i]);
        }
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
