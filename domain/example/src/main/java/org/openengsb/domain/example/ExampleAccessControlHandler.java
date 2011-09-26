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

package org.openengsb.domain.example;

import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.SpecialAccessControlHandler;
import org.openengsb.core.api.security.service.UserDataManager;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class ExampleAccessControlHandler implements SpecialAccessControlHandler {

    private UserDataManager userManager;

    @Override
    public boolean isAuthorized(String user, MethodInvocation invocation) {
        final String arg = (String) invocation.getArguments()[0];
        Collection<ExamplePermission> allPermissionsForUser =
            userManager.getAllPermissionsForUser(user, ExamplePermission.class);
        return Iterators.any(allPermissionsForUser.iterator(), new Predicate<ExamplePermission>() {
            @Override
            public boolean apply(ExamplePermission input) {
                String currentContext = ContextHolder.get().getCurrentContextId();
                String inputContext = input.getContext();
                if (inputContext != null && !inputContext.equals(currentContext)) {
                    return false;
                }
                return arg.startsWith(input.getAllowedPrefix());
            }
        });
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }
}
