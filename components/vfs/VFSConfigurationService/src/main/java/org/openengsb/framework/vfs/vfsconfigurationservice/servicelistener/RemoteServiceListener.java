/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener;


import org.openengsb.framework.vfs.configurationserviceapi.remoteservice.RemoteService;
import org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice.VFSConfigurationService;


import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class RemoteServiceListener
{
	private final Logger log = LoggerFactory.getLogger(RemoteServiceListener.class);
	private BundleContext context;
	private VFSConfigurationService vfsConfigurationService;
	private ServiceTracker<RemoteService, RemoteService> tracker;

	public RemoteServiceListener(BundleContext context, VFSConfigurationService vfsConfigurationService)
	{
		this.context = context;
		this.vfsConfigurationService = vfsConfigurationService;
	}

	public void open()
	{
		tracker = new ServiceTracker<RemoteService, RemoteService>(context, RemoteService.class, null)
		{
			@Override
			public RemoteService addingService(ServiceReference<RemoteService> reference)
			{
				RemoteService service = context.getService(reference);
				register(service);
				context.ungetService(reference);
				return service;
			}

			@Override
			public void removedService(ServiceReference<RemoteService> reference, RemoteService service)
			{
				//RepositoryHandler service = service;
				unregister(service);
			}
		};

		tracker.open();
	}

	public void register(RemoteService remoteService)
	{
		log.debug("add new RemoteService");
		vfsConfigurationService.registerRemoteService(remoteService);
	}

	public void unregister(RemoteService remoteService)
	{
		log.debug("remove RemoteService");
		vfsConfigurationService.unregisterRemoteService(remoteService);
	}

	public void close()
	{
		tracker.close();
	}
}