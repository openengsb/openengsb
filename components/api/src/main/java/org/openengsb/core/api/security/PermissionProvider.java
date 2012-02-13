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

import org.openengsb.core.api.ClassloadingDelegate;
import org.openengsb.core.api.security.model.Permission;

/**
 * Provides other bundles means of delegated class loading of permission-types and detecting which permission-classes
 * are available.
 * 
 * Every bundle declaring specific Permissions must export a service of this type to provide the declared permissions to
 * management bundles (user management, UI, ...)
 * 
 * Services exporting this interface must also declare the "permissionClass"-property where all supported classes are
 * listed.
 */
public interface PermissionProvider extends ClassloadingDelegate<Permission> {

}
