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

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * This class is intended for transporting username and password as authentication token for remote calls. The password
 * is provided to the authentication-manager as is. Make sure that the content of instances of this class is encrypted
 * when sending it over network. This is the responsibility of the corresponding filter-chain in the port.
 */
public class UsernamePasswordAuthenticationInfo implements AuthenticationInfo {

    private String username;
    private String password;

    public UsernamePasswordAuthenticationInfo(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public UsernamePasswordAuthenticationInfo() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Authentication toSpringSecurityAuthentication() {
        return new UsernamePasswordAuthenticationToken(username, password);
    }

}
