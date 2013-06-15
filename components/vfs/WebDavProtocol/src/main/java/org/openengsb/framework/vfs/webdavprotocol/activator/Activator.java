/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.webdavprotocol.activator;

import org.openengsb.framework.vfs.webdavprotocol.servicelistener.RepositoryHandlerListener;
import org.openengsb.framework.vfs.webdavprotocol.servicelistener.HttpServiceListener;
import org.openengsb.framework.vfs.webdavprotocol.webdavhandler.WebDavHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Richard
 */
public class Activator implements BundleActivator {

    private HttpServiceListener wrapp;
    private RepositoryHandlerListener confWrapp;
    private WebDavHandler webDavHandler = null;

    public void start(BundleContext bc) throws Exception {
        if (webDavHandler == null) {
            webDavHandler = WebDavHandler.getInstance();
        }

        webDavHandler.setBundleContext(bc);
        webDavHandler.start();
    }

    public void stop(BundleContext bc) throws Exception {
        if (webDavHandler != null) {
            webDavHandler.stopMilton();
        }
    }
}
