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

package org.openengsb.ui.common.wicket.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
@Documented
/**
 * Similar to Wicket's default SpringBean with the difference that the symbolic name of the bundle the bean is located
 * in is required. The injector will retrieve the spring bean from the correct bundle.
 */
public @interface OsgiSpringBean {

    /**
     * The name of the SpringBean defined in a spring context file.
     */
    String springBeanName();

    /**
     * The symbolic name of the bundle containing the spring bean definition.
     */
    String bundleSymbolicName();

}
