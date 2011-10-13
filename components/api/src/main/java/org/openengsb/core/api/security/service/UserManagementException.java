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

package org.openengsb.core.api.security.service;

/**
 * Generic UserManagementException. It serves as baseclass for other Checked UserManagementExceptions
 *
 * If this class is used itself, this can mean that something went wrong in the persistence-backend.
 */
public class UserManagementException extends Exception {

    private static final long serialVersionUID = 269605012169642052L;

    public UserManagementException() {
    }

    public UserManagementException(String message) {
        super(message);
    }

    public UserManagementException(Throwable cause) {
        super(cause);
    }

    public UserManagementException(String message, Throwable cause) {
        super(message, cause);
    }

}
