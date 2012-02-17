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
package org.openengsb.core.security.internal;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;

public final class RootSecurityHolder {

    private static AtomicReference<Subject> rootSubject = new AtomicReference<Subject>();

    public static void init() {
        DefaultSecurityManager defaultSecurityManager = new DefaultSecurityManager();
        defaultSecurityManager.setAuthenticator(new Authenticator() {
            @Override
            public AuthenticationInfo authenticate(AuthenticationToken authenticationToken)
                throws AuthenticationException {
                return new SimpleAuthenticationInfo(new Object(), null, "openengsb");
            }
        });
        Subject subject = defaultSecurityManager.createSubject(new DefaultSubjectContext());
        synchronized (rootSubject) {
            rootSubject.set(defaultSecurityManager.login(subject, null));
            rootSubject.notifyAll();
        }
    }

    public static Subject getRootSubject() {
        synchronized (rootSubject) {
            if (rootSubject.get() == null) {
                try {
                    rootSubject.wait();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
            return rootSubject.get();
        }
    }

    private RootSecurityHolder() {
    }
}
