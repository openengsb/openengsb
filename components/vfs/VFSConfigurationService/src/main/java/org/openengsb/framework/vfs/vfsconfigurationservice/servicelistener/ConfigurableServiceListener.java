/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener;


import org.openengsb.framework.vfs.configurationserviceapi.configurableservice.ConfigurableService;
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
public class ConfigurableServiceListener
{
	private final Logger log = LoggerFactory.getLogger(ConfigurableServiceListener.class);
	private BundleContext context;
	private VFSConfigurationService vfsConfigurationService;
	private ServiceTracker<ConfigurableService, ConfigurableService> tracker;

	public ConfigurableServiceListener(BundleContext bc, VFSConfigurationService vfsConfigurationService)
	{
		this.context = bc;
		this.vfsConfigurationService = vfsConfigurationService;
	}

	public void open()
	{
		tracker = new ServiceTracker<ConfigurableService, ConfigurableService>(context, ConfigurableService.class, null)
		{
			@Override
			public ConfigurableService addingService(ServiceReference<ConfigurableService> reference)
			{

				ConfigurableService service = context.getService(reference);
				register(service);
				context.ungetService(reference);
				return service;
			}

			@Override
			public void removedService(ServiceReference<ConfigurableService> reference, ConfigurableService service)
			{
				//RepositoryHandler service = service;
				unregister(service);
			}
		};

		tracker.open();
	}

	public void register(ConfigurableService configurableService)
	{
		log.debug("add new ConfigurableService");
		vfsConfigurationService.setConfigurableService(configurableService);
	}

	public void unregister(ConfigurableService configurableService)
	{
		log.debug("remove ConfigurableService");
		vfsConfigurationService.setConfigurableServiceLost(configurableService);
	}

	public void close()
	{
		tracker.close();
	}
}