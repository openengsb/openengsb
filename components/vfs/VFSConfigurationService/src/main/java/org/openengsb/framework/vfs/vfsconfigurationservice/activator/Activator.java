/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.vfsconfigurationservice.activator;

import org.openengsb.framework.vfs.configurationserviceapi.configurationservice.ConfigurationService;
import org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice.VFSConfigurationService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard
 */
public class Activator implements BundleActivator
{
	private final Logger logger = LoggerFactory.getLogger(Activator.class);
	private VFSConfigurationService vfsConfigurationService;
	private ConfigurationService configurationServiceRepository;

	public void start(BundleContext bc) throws Exception
	{
		if (vfsConfigurationService == null)
		{

			vfsConfigurationService = new VFSConfigurationService(bc);
			vfsConfigurationService.start();
			configurationServiceRepository = (ConfigurationService)vfsConfigurationService;
		}
		
		logger.debug("Register ConfigurationServiceRepository");
		bc.registerService(ConfigurationService.class.getName(), configurationServiceRepository, null);
	}

	public void stop(BundleContext bc) throws Exception
	{
		logger.debug("Stopping bundle VFSConfigurationService");
		if (vfsConfigurationService != null)
		{
			vfsConfigurationService.stop();
		}
	}
}
