/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.webdavprotocol.webdavhandler;

//import io.milton.servlet.MiltonServlet;
import io.milton.servlet.MiltonServlet;
import java.util.Hashtable;
import org.openengsb.framework.vfs.configurationserviceapi.repositoryhandler.RepositoryHandler;
import org.openengsb.framework.vfs.webdavprotocol.servicelistener.RepositoryHandlerListener;
import org.openengsb.framework.vfs.webdavprotocol.servicelistener.HttpServiceListener;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.openengsb.core.services.SecurityContext;

/**
 *
 * @author Richard
 */
public final class WebDavHandler {

    private Logger log = LoggerFactory.getLogger(WebDavHandler.class);
    private static WebDavHandler instance = new WebDavHandler();
    private BundleContext bundleContext;
    private HttpService httpService = null;
    private RepositoryHandler repositoryHandler = null;
    private MiltonServlet servlet;
    private HttpServiceListener httpServiceListener;
    private RepositoryHandlerListener repositoryHandlerListener;

    private WebDavHandler() {
    }

    public static WebDavHandler getInstance() {
        return instance;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void start() {
        log.debug("start webDavHandler");

        log.debug("start httpService listener");
        httpServiceListener = new HttpServiceListener(bundleContext, this);
        httpServiceListener.open();

        log.debug("start repositoryHandler listener");
        repositoryHandlerListener = new RepositoryHandlerListener(bundleContext, this);
        repositoryHandlerListener.open();

        //HttpServiceTracker hts = new HttpServiceTracker(bundleContext);
        //hts.open();
    }

    public void registerHttpService(HttpService httpService) {
        this.httpService = httpService;
        startMilton();
    }

    public void unregisterHttpService() {
        this.httpService = null;
        stopMilton();
    }

    public void registerRepositoryHandler(RepositoryHandler repositoryHandler) {
        this.repositoryHandler = repositoryHandler;
        startMilton();
    }

    public void unregisterRepositoryHandler() {
        this.repositoryHandler = null;
        stopMilton();
    }

    public RepositoryHandler getRepositoryHandler() {
        return this.repositoryHandler;
    }

    private void startMilton() {
        log.info("Starting milton");

        if (repositoryHandler == null) {
            log.error("repositoryHandler = null, return");
            return;
        }

        if (httpService == null) {
            log.error("HttpService = null, return");
            return;
        }

        servlet = new MiltonServlet();

        try {
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put("resource.factory.class",
                    "org.openengsb.framework.vfs.webdavprotocol.factories.ResourceFactoryImpl");
            httpService.registerServlet("/", servlet, props, null);

            log.info("Milton servlet registered");
        } catch (Exception e) {
            log.debug("register servlet failed: " + e.getMessage());
        }
    }

    public void stopMilton() {
        if (httpServiceListener != null) {
            httpServiceListener.close();
        }

        if (repositoryHandlerListener != null) {
            repositoryHandlerListener.close();
        }

        if (servlet != null) {
            servlet.destroy();
        }

        httpService = null;
        repositoryHandler = null;
    }
}
