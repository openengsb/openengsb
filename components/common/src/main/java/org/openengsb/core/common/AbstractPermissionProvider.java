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
package org.openengsb.core.common;

import org.openengsb.core.api.security.PermissionProvider;
import org.openengsb.core.api.security.model.Permission;

/**
 * Helps to provide a Simple PermissionProvider.
 * 
 * Just derive this class and add all permission-classes to the super-call in the default-constructor
 */
public abstract class AbstractPermissionProvider extends DefaultClassloadingDelegate<Permission> implements
        PermissionProvider {

    public AbstractPermissionProvider(Class<? extends Permission>... classes) {
        super(classes);
    }

}
