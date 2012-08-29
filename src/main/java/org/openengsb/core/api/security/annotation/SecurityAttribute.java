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
package org.openengsb.core.api.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigns a security-attribute to the target entity. These attributes are used by Access control connectors to
 * determine whether the user should be granted access.
 * 
 * It is also helpful for creating human readable names for service-interfaces, domain-interfaces, ...
 * 
 * You can annotate a method like this
 * 
 * <pre>
 * &#064;SecurityAttribute(&quot;example-method&quot;)
 * void example(String param, int param2);
 * </pre>
 * 
 * You can also define something other than a name:
 * 
 * <pre>
 * &#064;SecurityAttribute(key = &quot;domain.example.operationType&quot;, value = &quot;READ&quot;)
 * void getContent(String name);
 * </pre>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER })
public @interface SecurityAttribute {

    String key() default "name";

    String value();

}
