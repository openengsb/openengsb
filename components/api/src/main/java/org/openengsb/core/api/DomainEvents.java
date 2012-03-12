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

package org.openengsb.core.api;


/**
 * Base interface all domain event interfaces have to implement to be available in the OpenEngSB environment. This is a
 * marker interface, which should be extended by interfaces of the respective domains. The methods of these domain event
 * interfaces should be of the form {@code raiseEvent(MyDomainSpecificEvent e)}. The methods can have additional
 * parameters, but the first parameter has to be a subclass of the {@link Event} class. 
 */
public interface DomainEvents {
    
}
