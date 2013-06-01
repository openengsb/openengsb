package org.openengsb.framework.vfs.vfsrepositoryhandler.activator;

import org.openengsb.framework.vfs.configurationserviceapi.repositoryhandler.RepositoryHandler;
import org.openengsb.framework.vfs.vfsrepositoryhandler.VFSRepositoryHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Activator implements BundleActivator
{
	private final Logger logger = LoggerFactory.getLogger(Activator.class);
	private RepositoryHandler repositoryHandler = null;

	public void start(BundleContext bc) throws Exception
	{
		if(repositoryHandler == null)
		{
			VFSRepositoryHandler vfsRepositoryHandler = VFSRepositoryHandler.getInstance();
			vfsRepositoryHandler.setBundleContext(bc);
			vfsRepositoryHandler.start();
			
			repositoryHandler = vfsRepositoryHandler;
		}
	
		logger.debug("Register RepositoryHandler");
		bc.registerService(RepositoryHandler.class.getName(), repositoryHandler, null);
	}

	public void stop(BundleContext bc) throws Exception
	{
		logger.debug("Closing RepositoryHandler");
	}
}
