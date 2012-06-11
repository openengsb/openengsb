package org.openengsb.core.common.util;

import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

public final class FilterUtils {

    private FilterUtils() {
    }

    /**
     * creates a Filter, but wraps the {@link InvalidSyntaxException} into an {@link IllegalArgumentException}
     */
    public static Filter createFilter(String filterString) {
        try {
            return FrameworkUtil.createFilter(filterString);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Filter makeFilterForClass(Class<?> clazz) {
        return makeFilterForClass(clazz.getName());
    }

    public static Filter makeFilterForClass(String className) {
        try {
            return FrameworkUtil.createFilter(String.format("(%s=%s)", Constants.OBJECTCLASS, className));
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Filter makeFilter(Class<?> clazz, String otherFilter) throws IllegalArgumentException {
        return makeFilter(clazz.getName(), otherFilter);
    }

    public static Filter makeFilter(String className, String otherFilter) throws IllegalArgumentException {
        if (otherFilter == null) {
            return makeFilterForClass(className);
        }
        try {
            return FrameworkUtil.createFilter("(&" + makeFilterForClass(className) + otherFilter + ")");
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
