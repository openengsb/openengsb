package org.openengsb.core.security.internal;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;

public class RootSecurityHolder {

    private static Subject rootSubject;

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
        rootSubject = defaultSecurityManager.login(subject, null);
    }

    public static Subject getRootSubject() {
        return rootSubject;
    }
}
