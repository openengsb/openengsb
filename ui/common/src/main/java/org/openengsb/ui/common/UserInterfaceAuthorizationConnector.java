package org.openengsb.ui.common;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.core.common.util.CollectionUtils2;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.ui.common.model.UiPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class UserInterfaceAuthorizationConnector extends AbstractOpenEngSBConnectorService implements
        AuthorizationDomain {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInterfaceAuthorizationConnector.class);

    private UserDataManager userManager;

    @Override
    public Access checkAccess(String user, Object action) {
        if (action instanceof Class) {
            Class<?> componentClass = (Class<?>) action;
            return checkComponentInitializeAccess(user, componentClass);
        }
        return Access.ABSTAINED;
    }

    private Access checkComponentInitializeAccess(String user, Class<?> componentClass) {
        SecurityAttribute annotation = componentClass.getAnnotation(SecurityAttribute.class);
        if (annotation == null) {
            return Access.GRANTED;
        }
        final String securityAttribute = annotation.value();
        try {
            Collection<Permission> userPermissions =
                userManager.getUserPermissions(user, UiPermission.class.getName());
            Collection<UiPermission> uiPermissions =
                CollectionUtils2.filterCollectionByClass(userPermissions, UiPermission.class);
            boolean hasPermission = Iterators.any(uiPermissions.iterator(), new Predicate<UiPermission>() {
                @Override
                public boolean apply(UiPermission input) {
                    return ObjectUtils.equals(securityAttribute, input.getSecurityAttribute());
                }
            });
            if (hasPermission) {
                return Access.GRANTED;
            }
        } catch (UserNotFoundException e) {
            LOGGER.warn("user for authorization-descion not found", e);
            return Access.ABSTAINED;
        }
        return Access.ABSTAINED;
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

}
