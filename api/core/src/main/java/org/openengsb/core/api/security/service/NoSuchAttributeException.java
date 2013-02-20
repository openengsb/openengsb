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
 * This Exception is thrown by the {@link UserDataManager} when a non-existing attribute-value is queried.
 */
public class NoSuchAttributeException extends UserManagementException {

    private static final long serialVersionUID = 5068513969199836581L;

    public NoSuchAttributeException() {
    }

    public NoSuchAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchAttributeException(String message) {
        super(message);
    }

    public NoSuchAttributeException(Throwable cause) {
        super(cause);
    }

}
