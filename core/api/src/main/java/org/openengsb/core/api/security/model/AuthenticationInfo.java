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

package org.openengsb.core.api.security.model;

import org.springframework.security.core.Authentication;

/**
 * This interface serves as bridge between properly serializable AuthenticationInformation (compatible to
 * {@link org.openengsb.core.api.model.BeanDescription} and internal {@link Authentication}-tokens.
 *
 * When implementing authentication-providers for external authentication, this interface needs to be implemented by the
 * model-class representing the credentials.
 *
 * @see UsernamePasswordAuthenticationInfo
 */
public interface AuthenticationInfo {

    Authentication toSpringSecurityAuthentication();

}
