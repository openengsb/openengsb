package org.openengsb.core.ports.jms;

import org.openengsb.core.common.communication.MethodReturn;

public class ReturnMapping extends MethodReturn {

    private String className;

    @Override
    public final String getClassName() {
        return className;
    }

    public final void setClassName(String className) {
        this.className = className;
    }

}
