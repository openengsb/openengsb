/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.core.util;

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
