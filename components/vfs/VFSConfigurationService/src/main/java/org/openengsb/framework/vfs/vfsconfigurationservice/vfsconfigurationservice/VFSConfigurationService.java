/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;
import org.openengsb.framework.vfs.configurationserviceapi.configurableservice.ConfigurableService;
import org.openengsb.framework.vfs.configurationserviceapi.configurationservice.ConfigurationService;
import org.openengsb.framework.vfs.configurationserviceapi.remoteservice.RemoteService;
import org.openengsb.framework.vfs.configurationserviceapi.repositoryhandler.RepositoryHandler;
import org.openengsb.framework.vfs.vfsconfigurationservice.fileoperations.FileOperator;
import org.openengsb.framework.vfs.vfsconfigurationservice.fileoperations.VFSFileOperator;
import org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener.ConfigurableServiceListener;
import org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener.RemoteServiceListener;
import org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener.RepositoryHandlerListener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Richard Leopold
 */
public class VFSConfigurationService implements ConfigurationService
{
	private final Logger logger = LoggerFactory.getLogger(VFSConfigurationService.class);
	private RepositoryHandler repositoryHandler;
	private ResourceBundle configurationServiceProperties = ResourceBundle.getBundle("configurationservice");
	private ArrayList<RemoteService> remoteServices = new ArrayList<RemoteService>();
	private ArrayList<ConfigurableService> configurableServices = new ArrayList<ConfigurableService>();
	private RemoteServiceListener remoteServiceListener = null;
	private BundleContext bundleContext = null;
	private ConfigurableServiceListener configurableServiceListener = null;
	private RepositoryHandlerListener repositoryHandlerListener = null;
	private FileOperator fileOperator = new VFSFileOperator();

	public VFSConfigurationService(BundleContext bundleContext)
	{
		this.bundleContext = bundleContext;
	}

	public void newTag(Tag tag)
	{
		logger.debug("New tag arrived, reconfigure OpenEngSB");

		if (repositoryHandler == null)
		{
			logger.debug("Could not set new Tag");
			logger.debug("RspositoryHanlder not set yet, start bundle first");
			return;
		}

		//Stop all remoteServices
		for (RemoteService remote : remoteServices)
		{
			if (!remote.stop())
			{
				logger.debug("Could not set new Tag");
				logger.debug("problem stopping remoteService, new Tag will not be deployed");
				return;
			}
		}

		File tempFolder = new File("temp");

		if (!tempFolder.exists() || !tempFolder.isDirectory())
		{
			try
			{
				Files.createDirectories(tempFolder.toPath());
			}
			catch (IOException ex)
			{
				logger.debug("error creating temp file");
			}
		}

		String configurationFolderPath = configurationServiceProperties.getString("configurationPath");

		File configFolder = new File(configurationFolderPath);

		if (!configFolder.exists())
		{
			try
			{
				Files.createDirectories(configFolder.toPath());
			}
			catch (IOException ex)
			{
				logger.debug("error creating configuration directory: " + ex.getMessage());
				return;
			}
		}

		for (File f : configFolder.listFiles())
		{
			fileOperator.fileDelete(f);
		}
		
		fileOperator.copyDirectory(tag.getPath().toFile(), configFolder);

		List<String> changes = fileOperator.compareFolders(tempFolder, configFolder);

		for (ConfigurableService service : configurableServices)
		{
			for (String s : service.getPropertyList())
			{
				if (changes.contains(s))
				{
					//reconfigure Service and check if reconfigure was successful
					if (!service.reconfigure())
					{
						loadPreviousTag(tag);
						//When the previous config was start successful return and end it. 
						return;
					}
					break;
				}
			}
		}


		//Restart all remoteServices
		for (RemoteService remote : remoteServices)
		{
			if (!remote.start())
			{
				logger.debug("error restart remoteService -- reload previous config");
				loadPreviousTag(tag);
				//When the previous config was start successful return and end it. 
				return;
			}
		}

		logger.debug("Successfully extracted Tag to configurationfolder");
	}

	//Load the previous configuration and set the configuration
	private void loadPreviousTag(Tag actualTag)
	{
		logger.debug("Error starting openEngSB woth the new Configuration");
		logger.debug("Geting old config and try again");
		newTag(repositoryHandler.getPreviousTag(actualTag));
	}

	public void start()
	{
		configurableServiceListener = new ConfigurableServiceListener(bundleContext, this);
		configurableServiceListener.open();
		remoteServiceListener = new RemoteServiceListener(bundleContext, this);
		remoteServiceListener.open();
		repositoryHandlerListener = new RepositoryHandlerListener(bundleContext, this);
		repositoryHandlerListener.open();
	}

	public void stop()
	{
		configurableServiceListener.close();
		remoteServiceListener.close();
		repositoryHandlerListener.close();
	}

	public void registerRepositoryHandler(RepositoryHandler repositoryhandler)
	{
		this.repositoryHandler = repositoryhandler;
	}

	public void unregisterRepositoryHandler()
	{
		this.repositoryHandler = null;
	}

	public void registerRemoteService(RemoteService remoteService)
	{
		if (!this.remoteServices.contains(remoteService))
		{
			remoteServices.add(remoteService);
		}
	}

	public void unregisterRemoteService(RemoteService remoteService)
	{
		if (this.remoteServices.contains(remoteService))
		{
			remoteServices.remove(remoteService);
		}
	}

	public void setConfigurableService(ConfigurableService configurableService)
	{
		if (!configurableServices.contains(configurableService))
		{
			configurableServices.add(configurableService);
		}
	}

	public void setConfigurableServiceLost(ConfigurableService configurableService)
	{
		if (configurableServices.contains(configurableService))
		{
			configurableServices.remove(configurableService);
		}
	}
}