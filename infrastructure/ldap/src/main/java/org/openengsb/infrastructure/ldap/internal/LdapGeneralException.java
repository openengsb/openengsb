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

package org.openengsb.infrastructure.ldap.internal;

import org.apache.directory.shared.ldap.model.exception.LdapException;

/**
 * Indicates that the dao communicating with the Ldap server ran into a general error or an error it can not recover.
 * Most of the time this exception will wrap a {@link LdapException}.
 * */
public class LdapGeneralException extends RuntimeException {

    private static final long serialVersionUID = -5589147032700131423L;

    public LdapGeneralException() {
    }

    public LdapGeneralException(String message) {
        super(message);
    }

    public LdapGeneralException(Throwable cause) {
        super(cause);
    }

    public LdapGeneralException(String message, Throwable cause) {
        super(message, cause);
    }

}
