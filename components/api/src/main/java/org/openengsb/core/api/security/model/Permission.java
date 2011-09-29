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
package org.openengsb.core.api.security.model;

/**
 * Baseclass for Permissions used for Access control in OpenEngSB.
 *
 * Permissions can be stored and managed using a {@link org.openengsb.core.api.security.service.UserDataManager}
 * service. In order to save and read permissions again, a permission must be designed as a plain Java Object. All
 * property types must fulfill certain requirements:
 * <ul>
 * <li>a constructor with exactly one argument of type {@link String}</li>
 * <li>the {@link Object#toString()} must create a string-representation that can be used with that constructor the
 * recreate the object</li>
 * </ul>
 *
 * Collections of values are also allowed. However the types of the values underly the same constraints as single
 * values.
 */
public interface Permission {

    /**
     * return a description of what this permission object permits a user to do (filling in what the values of the
     * argument mean).
     *
     * Example: Allows the user to perform the operation "doSomething" on all services of type "example".
     */
    String describe();

}
