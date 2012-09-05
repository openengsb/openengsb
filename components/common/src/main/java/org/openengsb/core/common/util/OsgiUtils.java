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

package org.openengsb.core.common.util;

import org.openengsb.core.api.context.ContextHolder;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

/**
 * The OsgiUtils class contains bundle context independent OSGi related methods 
 */
public class OsgiUtils {
    
    private OsgiUtils() {
    }

    /**
     * returns a filter that matches services with the given class and location in both the given context and the
     * root-context
     *
     * @throws IllegalArgumentException if the location contains special characters that prevent the filter from
     *         compiling
     */
    public static Filter getFilterForLocation(Class<?> clazz, String location, String context)
        throws IllegalArgumentException {
        String filter = makeLocationFilterString(location, context);
        return FilterUtils.makeFilter(clazz, filter);
    }

    /**
     * returns a filter that matches services with the given class and location in both the current context and the
     * root-context
     *
     * @throws IllegalArgumentException if the location contains special characters that prevent the filter from
     *         compiling
     */
    public static Filter getFilterForLocation(Class<?> clazz, String location) throws IllegalArgumentException {
        return getFilterForLocation(clazz, location, ContextHolder.get().getCurrentContextId());
    }

    /**
     * returns a filter that matches services with the given location in both the given context and the root-context
     *
     * @throws IllegalArgumentException if the location contains special characters that prevent the filter from
     *         compiling
     */
    public static Filter getFilterForLocation(String location, String context) throws IllegalArgumentException {
        String filter = makeLocationFilterString(location, context);
        try {
            return FrameworkUtil.createFilter(filter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("location is invalid: " + location, e);
        }
    }

    /**
     * returns a filter that matches services with the given location in both the current context and the root-context
     *
     * @throws IllegalArgumentException if the location contains special characters that prevent the filter from
     *         compiling
     */
    public static Filter getFilterForLocation(String location) throws IllegalArgumentException {
        return getFilterForLocation(location, ContextHolder.get().getCurrentContextId());
    }

    private static String makeLocationFilterString(String location, String context) throws IllegalArgumentException {
        return String.format("(|(location.%s=%s)(location.root=%s))", context, location, location);
    }
}
