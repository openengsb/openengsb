package org.openengsb.ports.jms;

public interface TestInterface {
    String method(String arg);

    TestClass method(String arg1, Integer arg2, TestClass arg3);
}
