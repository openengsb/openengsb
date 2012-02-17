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
package org.openengsb.ui.admin;

import java.util.Collection;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;

public class OpenEngSBWebSecurityManager extends DefaultWebSecurityManager {

    private static WebSecurityManager instance;

    public OpenEngSBWebSecurityManager() {
    }

    public OpenEngSBWebSecurityManager(Collection<Realm> realms) {
        super(realms);
    }

    public OpenEngSBWebSecurityManager(Realm singleRealm) {
        super(singleRealm);
    }

    public void init() {
        instance = this;
    }

    public static WebSecurityManager get() {
        return instance;
    }

}
