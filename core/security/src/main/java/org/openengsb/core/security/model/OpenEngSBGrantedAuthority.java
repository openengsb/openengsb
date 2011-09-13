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

import org.openengsb.core.api.security.model.Permission;
import org.springframework.security.core.GrantedAuthority;

/**
 * serves as a bridge between openengsb's {@link Permission}s and spring-security's {@link GrantedAuthority}s
 *
 * A simple implementation is {@link PermissionAuthority} class, which wraps a single {@link Permission} into a
 * {@link GrantedAuthority}
 */
public interface OpenEngSBGrantedAuthority extends GrantedAuthority {

    /**
     * return all permission of the wrapped OpenEngSB authority
     */
    Collection<? extends Permission> getPermissions();

}
