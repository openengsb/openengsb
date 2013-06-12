package org.openengsb.framework.vfs.webdavprotocol.servicelistener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.openengsb.framework.vfs.webdavprotocol.webdavhandler.WebDavHandler;

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
public class HttpServiceListener
{

	private final Logger log = LoggerFactory.getLogger(HttpServiceListener.class);
	private BundleContext context;
	private WebDavHandler webDavHandler;
	private ServiceTracker<HttpService, HttpService> tracker;

	public HttpServiceListener(BundleContext context, WebDavHandler webDavHandler)
	{
		this.context = context;
		this.webDavHandler = webDavHandler;
	}

	public void open()
	{
		tracker = new ServiceTracker<HttpService, HttpService>(context, HttpService.class, null)
		{
			@Override
			public HttpService addingService(ServiceReference<HttpService> reference)
			{

				HttpService service = context.getService(reference);
				register(service);
				return service;
			}

			@Override
			public void removedService(ServiceReference<HttpService> reference, HttpService service)
			{
				unregister(service);
			}
		};
		tracker.open();
	}

	public void register(HttpService httpService)
	{
		log.debug("add new HttpService");
		webDavHandler.registerHttpService(httpService);
	}

	public void unregister(HttpService httpService)
	{
		log.debug("remove HttpService");
		webDavHandler.unregisterHttpService();
	}

	public void close()
	{
		tracker.close();
	}
}