package org.openengsb.ui.common;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.SpringSecurityContext;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainAuthorizationStrategy implements IAuthorizationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainAuthorizationStrategy.class);

    private AuthorizationDomain authorizer = OpenEngSBCoreServices.getWiringService().getDomainEndpoint(
        AuthorizationDomain.class, "authorization");

    @Override
    public boolean isActionAuthorized(Component arg0, Action arg1) {
        return true;
    }

    @Override
    public <T extends Component> boolean isInstantiationAuthorized(Class<T> componentClass) {
        SecurityAttribute annotation = componentClass.getAnnotation(SecurityAttribute.class);
        if (annotation == null) {
            return true;
        }
        LOGGER.debug("security-attribute-annotation present");
        Authentication authentication = SpringSecurityContext.getInstance().getAuthentication();
        if (authentication == null) {
            return false;
        }
        String user = authentication.getUsername();
        return authorizer.checkAccess(user, componentClass) == Access.GRANTED;
    }

    public void setAuthorizer(AuthorizationDomain authorizer) {
        this.authorizer = authorizer;
    }
}
