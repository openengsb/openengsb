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

import org.apache.directory.shared.ldap.model.name.Dn;

/**
 * Thrown upon trying to access a non-existing {@link Dn}. This exception indicates that the Dn can be resolved until
 * the immediate ancestor of its leaf. If resolving a Dn already fails at a level above its leaf, a
 * {@link MissingParentException} should be thrown.
 * */
public class NoSuchNodeException extends InconsistentDITException {

    private static final long serialVersionUID = 4929321966265341536L;

    private final Dn dn;

    /**
     * Pass the {@link Dn} whose leaf is missing in the DIT.
     */
    public NoSuchNodeException(Dn dn) {
        super();
        this.dn = dn;
    }

    /**
     * Pass the {@link Dn} whose leaf is missing in the DIT.
     */
    public NoSuchNodeException(String message, Dn dn) {
        super(message);
        this.dn = dn;
    }

    /**
     * Pass the {@link Dn} whose leaf is missing in the DIT.
     */
    public NoSuchNodeException(Throwable cause, Dn dn) {
        super(cause);
        this.dn = dn;
    }

    /**
     * Pass the {@link Dn} whose leaf is missing in the DIT.
     */
    public NoSuchNodeException(String message, Throwable cause, Dn dn) {
        super(message, cause);
        this.dn = dn;
    }

    /**
     * Pass the {@link Dn} whose leaf is missing in the DIT.
     */
    public Dn getDn() {
        return dn;
    }

}
