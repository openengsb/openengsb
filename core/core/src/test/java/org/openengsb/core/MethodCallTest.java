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

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;

public class MethodCallTest {

    @Test
    public void testCall() throws InvocationFailedException {
        Object[] args = new Object[] { "foo", 42, new Object() };
        MethodCall methodCall = new MethodCall("set", args, new Class<?>[] { String.class, int.class, Object.class });
        Bean bean = new Bean();
        ReturnValue returnValue = methodCall.invoke(bean);

        Assert.assertEquals(args[0], bean.a);
        Assert.assertEquals(args[1], bean.b);
        Assert.assertEquals(args[2], bean.c);
        Assert.assertEquals("success", returnValue.getValue());
        Assert.assertEquals(String.class, returnValue.getType());
    }

    public static class Bean {
        private String a;
        private int b;
        private Object c;

        public String set(String a, int b, Object c) {
            this.a = a;
            this.b = b;
            this.c = c;
            return "success";
        }
    }
}
