/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.webdavprotocol.servicelistener;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class HttpServiceTracker extends ServiceTracker {

    private final Logger logger = LoggerFactory.getLogger(HttpServiceTracker.class);

    public HttpServiceTracker(BundleContext context) {
        super(context, HttpService.class.getName(), null);
    }

    @Override
    public Object addingService(ServiceReference reference) {
        logger.info("HttpServiceTracker found new HttpService");
        HttpService http = (HttpService) super.addingService(reference);
        return http;
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        logger.info("HttpServiceTracker unregistered HttpService");
        super.removedService(reference, service);
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        logger.info("HttpServiceTracker modified HttpService");
        super.modifiedService(reference, service);
    }
}
