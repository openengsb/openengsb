package org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice;

import java.io.File;
import java.util.Date;
import javax.swing.text.html.HTML;
import org.junit.Test;
import org.junit.Assert;
import org.openengsb.framework.vfs.configurationserviceapi.repositoryhandler.RepositoryHandler;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;
import org.openengsb.framework.vfs.configurationserviceapi.configurableservice.ConfigurableService;
import org.openengsb.framework.vfs.configurationserviceapi.remoteservice.RemoteService;
import org.osgi.framework.BundleContext;

public class VFSConfigurationServiceTest {
    @Test
	public void testNewTagEverythingOk()
	{
		BundleContext bundleContext = mock(BundleContext.class);
		
		RepositoryHandler repositoryHandler = mock(RepositoryHandler.class);
		
		ConfigurableService configurableService1 = mock(ConfigurableService.class);
		when(configurableService1.reconfigure()).thenReturn(true);
		
		ConfigurableService configurableService2 = mock(ConfigurableService.class);
		when(configurableService2.reconfigure()).thenReturn(true);
		
		RemoteService remoteService1 = mock(RemoteService.class);
		when(remoteService1.stop()).thenReturn(true);
		when(remoteService1.start()).thenReturn(true);
		
		RemoteService remoteService2 = mock(RemoteService.class);
		when(remoteService2.stop()).thenReturn(true);
		when(remoteService2.start()).thenReturn(true);
		
		VFSConfigurationService configurationService = new VFSConfigurationService(bundleContext);
		configurationService.start();
		
		Date date = new Date(0);
		Tag tag = mock(Tag.class);
		when(tag.getDate()).thenReturn(date);
		when(tag.getName()).thenReturn("testTag");
		when(tag.getPath()).thenReturn((new File("testTagPath")).toPath());
		
		configurationService.newTag(tag);
	}
}