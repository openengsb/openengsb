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

package org.openengsb.core.security.model;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public abstract class OpenEngSBGrantedAuthority implements GrantedAuthority {

    private static final long serialVersionUID = -1763672424492282903L;

    // null because it needs to be handled in a specific AccessDecisionVoter
    @Override
    public String getAuthority() {
        return null;
    }

    public abstract Collection<Permission> getPermissions();

}
