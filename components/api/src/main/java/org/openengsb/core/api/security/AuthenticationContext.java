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

import java.util.List;

/**
 * Provides methods for authentication and determining who is currently authenticated
 */
public interface AuthenticationContext {

    /**
     * Authenticates the user and binds that authentication to the current Thread. Subsequent calls to
     * getAllAuthenticatedPrincipals or getAuthenticatedPrincipal must return the principals of the user authenticated
     * here until logout is called.
     * New Threads spawned from the current thread must inherit the authentication
     */
    void login(String username, Credentials credentials);

    /**
     * Invalidates the authentication bound to the current Thread and all threads spawned from that thread.
     */
    void logout();

    /**
     * returns the principal of the currently authenticated user or null if no authentication was bound to the current
     * Thread
     */
    Object getAuthenticatedPrincipal();

    /**
     * returns all principals of the currently authenticated user. If no authentication was bound to the current
     * Thread the list is empty.
     */
    List<Object> getAllAuthenticatedPrincipals();

}
