/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.ReturnValueTransformerTest.TestBeanArray;
import org.openengsb.core.model.Event;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class MethodCallTransformerTest {

    @Test
    public void testNoArg() throws SerializationException {
        MethodCall input = new MethodCall("getAllValues", new Object[] { "path" }, new Class<?>[] { String.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        check(input, output);
    }

    @Test
    public void testPrimitive() throws SerializationException {
        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, "hallo" }, new Class<?>[] { Integer.class,
                Long.class, String.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        check(input, output);
    }

    @Test
    public void testList() throws SerializationException {
        List<Integer> intList = new ArrayList<Integer>();
        intList.add(42);
        intList.add(43);

        List<String> stringList = new ArrayList<String>();
        stringList.add("42");
        stringList.add("43");

        MethodCall input = new MethodCall("foo", new Object[] { intList, stringList }, new Class<?>[] { List.class,
                List.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        Assert.assertEquals(intList, output.getArgs()[0]);
        Assert.assertEquals(stringList, output.getArgs()[1]);
    }

    @Test
    public void testBean() throws SerializationException {
        TestBean beanA = new TestBean("testStringA", 42, null);
        TestBean beanB = new TestBean("testStringB", 3, beanA);
        beanA.setBean(beanB);

        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, beanA }, new Class<?>[] { Integer.class,
                Long.class, TestBean.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        check(input, output);

        TestBean tbA = (TestBean) output.getArgs()[2];
        TestBean tbB = tbA.getBean();

        Assert.assertTrue(tbA == tbB.getBean());
    }

    @Test
    public void testSelfReferencingBean() throws SerializationException {
        TestBean beanA = new TestBean("bar", 42, null);
        beanA.setBean(beanA);

        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, beanA }, new Class<?>[] { Integer.class,
                Long.class, TestBean.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        check(input, output);

        TestBean tbA = (TestBean) output.getArgs()[2];
        TestBean tbB = tbA.getBean();

        Assert.assertTrue(tbA == tbB.getBean());
    }

    @Test
    public void testBeanWithList() throws Exception {
        TestBeanList testBean = new TestBeanList();
        testBean.addTestData();

        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, testBean }, new Class<?>[] { Integer.class,
                Long.class, TestBeanList.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        TestBeanList tbList = (TestBeanList) output.getArgs()[2];
        TestBean tbA = tbList.getTestBeanList().get(0);
        TestBean tbB = tbList.getTestBeanList().get(1);

        Assert.assertTrue(tbA == tbB.getBean());
    }

    @Test
    public void testBeanWithTwoEqualLists() throws Exception {
        TestBeanList2 testBean = new TestBeanList2();
        testBean.addTestData();

        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, testBean }, new Class<?>[] { Integer.class,
                Long.class, TestBeanList2.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        check(input, output);

        TestBeanList2 tbArray = (TestBeanList2) output.getArgs()[2];
        List<String> list = tbArray.getList();
        List<String> list2 = tbArray.getList2();

        Assert.assertTrue(list == list2);
    }

    @Test
    public void testPrimitiveClasses() throws Exception {
        TestBeanArray testBean = new TestBeanArray();
        testBean.addTestData();

        MethodCall input = new MethodCall("foo", new Object[] { new Integer(42), new Byte("42"), new Short((short) 42),
                new Long(42L), new Float(42.42), new Double(42.42), new Boolean(true) }, new Class<?>[] {
                Integer.class, Byte.class, Short.class, Long.class, Float.class, Double.class, Boolean.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        check(input, output);
    }

    @Test
    public void testEvent() throws Exception {
        Event event = new Event("domain", "name");

        MethodCall input = new MethodCall("foo", new Object[] { event }, new Class<?>[] { Event.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        check(input, output);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEventList() throws Exception {
        List<Event> events = Arrays.asList(new Event("domain", "name"));

        MethodCall input = new MethodCall("foo", new Object[] { events }, new Class<?>[] { List.class });

        String xml = Transformer.toXml(input);
        MethodCall output = Transformer.toMethodCall(xml);

        List<Event> outEvents = (List<Event>) output.getArgs()[0];
        Assert.assertEquals(events, outEvents);
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
        private Integer i;
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
            result = prime * result + ((i == null) ? 0 : i.hashCode());
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
            if (i == null) {
                if (other.i != null)
                    return false;
            } else if (!i.equals(other.i))
                return false;
            if (string == null) {
                if (other.string != null)
                    return false;
            } else if (!string.equals(other.string))
                return false;
            return true;
        }

    }

    public static class TestBeanList {
        private List<Integer> intList;

        private List<TestBean> testBeanList;

        public TestBeanList() {
        }

        public void addTestData() {
            intList = Arrays.asList(42, 1, 2, 3);
            TestBean testBeanA = new TestBean("foo", 4, null);
            testBeanList = Arrays.asList(testBeanA, new TestBean("bar", 5, testBeanA));
        }

        public List<Integer> getIntList() {
            return intList;
        }

        public List<TestBean> getTestBeanList() {
            return testBeanList;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((intList == null) ? 0 : intList.hashCode());
            result = prime * result + ((testBeanList == null) ? 0 : testBeanList.hashCode());
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
            TestBeanList other = (TestBeanList) obj;
            if (intList == null) {
                if (other.intList != null)
                    return false;
            } else if (!intList.equals(other.intList))
                return false;
            if (testBeanList == null) {
                if (other.testBeanList != null)
                    return false;
            } else if (!testBeanList.equals(other.testBeanList))
                return false;
            return true;
        }

    }

    public static class TestBeanList2 {
        private List<String> list;

        private List<String> list2;

        public TestBeanList2() {
        }

        public void addTestData() {
            list = new ArrayList<String>();
            list2 = list;
        }

        public List<String> getList() {
            return list;
        }

        public List<String> getList2() {
            return list2;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((list == null) ? 0 : list.hashCode());
            result = prime * result + ((list2 == null) ? 0 : list2.hashCode());
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
            TestBeanList2 other = (TestBeanList2) obj;
            if (list == null) {
                if (other.list != null)
                    return false;
            } else if (!list.equals(other.list))
                return false;
            if (list2 == null) {
                if (other.list2 != null)
                    return false;
            } else if (!list2.equals(other.list2))
                return false;
            return true;
        }

    }
}
