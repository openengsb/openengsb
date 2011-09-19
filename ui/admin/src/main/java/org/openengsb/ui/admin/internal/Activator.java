package org.openengsb.ui.admin.internal;

import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        UserDataManager userManager = OpenEngSBCoreServices.getServiceUtilsService().getService(UserDataManager.class);
        if (!userManager.getAllPermissionsFromSet("ROLE_USER").isEmpty()) {
            return;
        }
        userManager.addPermissionToSet("ROLE_USER", new WicketPermission("SERVICE_USER"));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub

    }

}
