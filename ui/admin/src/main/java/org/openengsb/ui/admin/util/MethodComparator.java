package org.openengsb.ui.admin.util;

import java.lang.reflect.Method;
import java.util.Comparator;

public final class MethodComparator implements Comparator<Method> {
    @Override
    public int compare(Method o1, Method o2) {
        return o1.toString().compareTo(o2.toString());
    }
}
