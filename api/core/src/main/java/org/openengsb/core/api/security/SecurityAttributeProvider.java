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
package org.openengsb.core.api.security;

import java.util.Collection;

import org.openengsb.core.api.security.model.SecurityAttributeEntry;

/**
 * A service that can serve as a source for SecurityAttributes. While Annotation only allow assigning attributes at
 * compile-time, these services can be used to assign attributes to objects at runtime.
 *
 * Every Bundle that wants to store security-attributes should register its own provider. So when the bundle is
 * restarted, the attributes can be handled accordingly.
 *
 * Example: A UI-bundle wants to store attributes on instances of UI-components. This means, the bundle itself must
 * provide a service with this interface and make sure it's populated with the data as desired.
 */
public interface SecurityAttributeProvider {

    /**
     * returns all attributes associated with the given object in the context of this specific provider
     */
    Collection<SecurityAttributeEntry> getAttribute(Object o);

}
