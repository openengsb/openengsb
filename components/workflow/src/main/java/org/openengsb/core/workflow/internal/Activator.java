package org.openengsb.core.workflow.internal;

import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.workflow.OsgiHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        OsgiHelper.setUtilsService(new DefaultOsgiUtilsService(context));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}
