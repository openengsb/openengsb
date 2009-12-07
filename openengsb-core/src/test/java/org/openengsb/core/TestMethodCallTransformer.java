package org.openengsb.core;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.messaging.Segment;

public class TestMethodCallTransformer {

    @Test
    public void test() {
        MethodCall input = new MethodCall("foo", new Object[] { 1, 42L, "hallo" },
                new Class<?>[] { int.class, long.class, String.class });

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
}
