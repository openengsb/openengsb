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

package org.openengsb.core.common.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public final class Users {

    public static User create(String username, String password, Collection<GrantedAuthority> authorities) {
        return new User(username, password, true, true, true, true, authorities);
    }

    public static User create(String username, String password, GrantedAuthority... authorities) {
        return create(username, password, Arrays.asList(authorities));
    }

    public static User create(String username, String password) {
        List<GrantedAuthority> emptyList = Collections.emptyList();
        return create(username, password, emptyList);
    }

    private Users() {
    }
}
