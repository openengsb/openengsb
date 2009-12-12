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

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.messaging.Segment;
import org.openengsb.core.methodcalltransformation.MethodCall;
import org.openengsb.core.methodcalltransformation.MethodCallTransformer;
import org.openengsb.util.serialization.SerializationException;

public class TestMethodCallTransformer {

    @Test
    public void testPrimitive() {
        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, "hallo" }, new Class<?>[] { int.class,
                long.class, String.class });

        Segment intermediate = MethodCallTransformer.transform(input);
        MethodCall output = MethodCallTransformer.transform(intermediate);

        Assert.assertEquals(input.getMethodName(), output.getMethodName());
        Assert.assertEquals(input.getArgs().length, output.getArgs().length);
        Assert.assertEquals(input.getTypes().length, output.getTypes().length);
        Assert.assertEquals(output.getArgs().length, output.getTypes().length);

        for (int i = 0; i < input.getArgs().length; i++) {
            Assert.assertEquals(input.getTypes()[i], output.getTypes()[i]);
            Assert.assertEquals(input.getArgs()[i], output.getArgs()[i]);
        }
    }

    @Test
    public void testBean() throws SerializationException {
        TestBean beanA = new TestBean();
        TestBean beanB = new TestBean();
        beanA.setBean(beanB);
        beanB.setBean(beanA);
        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, beanA }, new Class<?>[] { int.class,
                long.class, TestBean.class });

        Segment intermediate = MethodCallTransformer.transform(input);
        MethodCall output = MethodCallTransformer.transform(intermediate);

        Assert.assertEquals(input.getMethodName(), output.getMethodName());
        Assert.assertEquals(input.getArgs().length, output.getArgs().length);
        Assert.assertEquals(input.getTypes().length, output.getTypes().length);
        Assert.assertEquals(output.getArgs().length, output.getTypes().length);

        for (int i = 0; i < input.getArgs().length; i++) {
            Assert.assertEquals(input.getTypes()[i], output.getTypes()[i]);
            Assert.assertEquals(input.getArgs()[i], output.getArgs()[i]);
        }

        TestBean tbA = (TestBean) output.getArgs()[2];
        TestBean tbB = tbA.getBean();

        Assert.assertTrue(tbA == tbB.getBean());
    }

    @Test
    public void testSelfReferencingBean() throws SerializationException {
        TestBean beanA = new TestBean();
        beanA.setBean(beanA);
        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, beanA }, new Class<?>[] { int.class,
                long.class, TestBean.class });

        Segment intermediate = MethodCallTransformer.transform(input);
        MethodCall output = MethodCallTransformer.transform(intermediate);

        Assert.assertEquals(input.getMethodName(), output.getMethodName());
        Assert.assertEquals(input.getArgs().length, output.getArgs().length);
        Assert.assertEquals(input.getTypes().length, output.getTypes().length);
        Assert.assertEquals(output.getArgs().length, output.getTypes().length);

        for (int i = 0; i < input.getArgs().length; i++) {
            Assert.assertEquals(input.getTypes()[i], output.getTypes()[i]);
            Assert.assertEquals(input.getArgs()[i], output.getArgs()[i]);
        }

        TestBean tbA = (TestBean) output.getArgs()[2];
        TestBean tbB = tbA.getBean();

        Assert.assertTrue(tbA == tbB.getBean());
    }

    public static class TestBean {
        private String string = "hoho";
        private int i = 42;
        private TestBean bean;

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
}
