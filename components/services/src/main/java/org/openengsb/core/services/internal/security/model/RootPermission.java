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
package org.openengsb.core.services.internal.security.model;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.labs.delegation.service.Provide;

/**
 * When a user is granted an instance of this permission, every action and access is allowed. This is ensured by the
 * {@link org.openengsb.core.services.internal.security.AdminAccessConnector}.
 */
@Provide(context = Constants.DELEGATION_CONTEXT_PERMISSIONS)
public class RootPermission implements Permission {
    @Override
    public String describe() {
        return "grants ALL permissions, thus providing ROOT-access";
    }
}
