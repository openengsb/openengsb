package org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.framework.vfs.api.common.Tag;
import org.openengsb.framework.vfs.api.configurableservice.ConfigurableService;
import org.openengsb.framework.vfs.api.remoteservice.RemoteService;
import org.openengsb.framework.vfs.api.repositoryhandler.RepositoryHandler;
import org.openengsb.framework.vfs.vfsconfigurationservice.fileoperations.FileOperator;
import org.osgi.framework.BundleContext;

public class VFSConfigurationServiceTest {
    private ResourceBundle configurationServiceProperties = ResourceBundle.getBundle("configurationservice");
    private String configurationPath = configurationServiceProperties.getString("configurationPath");
    
    @Test
    public void testNewTagEverythingOk() {
        BundleContext bundleContext = Mockito.mock(BundleContext.class);

        RepositoryHandler repositoryHandler = Mockito.mock(RepositoryHandler.class);

        ConfigurableService configurableService1 = Mockito.mock(ConfigurableService.class);
        Mockito.when(configurableService1.reconfigure()).thenReturn(true);
        
        List<String> configurableService1Configs = new ArrayList<>();
        configurableService1Configs.add(configurationPath + "/folder1/config2.txt");
        Mockito.when(configurableService1.getPropertyList()).thenReturn(configurableService1Configs);

        ConfigurableService configurableService2 = Mockito.mock(ConfigurableService.class);
        Mockito.when(configurableService2.reconfigure()).thenReturn(true);
        
        List<String> configurableService2Configs = new ArrayList<>();
        configurableService2Configs.add(configurationPath + "/config3.txt");
        Mockito.when(configurableService2.getPropertyList()).thenReturn(configurableService2Configs);
        
        ConfigurableService configurableService3 = Mockito.mock(ConfigurableService.class);
        Mockito.when(configurableService3.reconfigure()).thenReturn(true);
        
        List<String> configurableService3Configs = new ArrayList<>();
        configurableService3Configs.add(configurationPath + "/folder1");
        Mockito.when(configurableService3.getPropertyList()).thenReturn(configurableService3Configs);
        
        ConfigurableService configurableService4 = Mockito.mock(ConfigurableService.class);
        Mockito.when(configurableService4.reconfigure()).thenReturn(true);
        
        List<String> configurableService4Configs = new ArrayList<>();
        configurableService4Configs.add(configurationPath + "/config1.txt");
        Mockito.when(configurableService4.getPropertyList()).thenReturn(configurableService4Configs);

        RemoteService remoteService1 = Mockito.mock(RemoteService.class);
        Mockito.when(remoteService1.stop()).thenReturn(true);
        Mockito.when(remoteService1.start()).thenReturn(true);

        RemoteService remoteService2 = Mockito.mock(RemoteService.class);
        Mockito.when(remoteService2.stop()).thenReturn(true);
        Mockito.when(remoteService2.start()).thenReturn(true);
        
        FileOperator fileOperator = Mockito.mock(FileOperator.class);
        
        List<String> changedConfigs = new ArrayList<>();
        changedConfigs.add(configurationPath + "/config1.txt");
        changedConfigs.add(configurationPath + "/folder1/config2.txt");
        Mockito.when(fileOperator.compareFolders(Mockito.any(File.class), Mockito.any(File.class)))
                .thenReturn(changedConfigs);
        Mockito.when(fileOperator.listFiles(new File(configurationPath))).thenReturn(new File[0]);

        VFSConfigurationService configurationService = new VFSConfigurationService(bundleContext);
        configurationService.start();
        configurationService.registerRemoteService(remoteService1);
        configurationService.registerRemoteService(remoteService2);
        configurationService.setConfigurableService(configurableService1);
        configurationService.setConfigurableService(configurableService2);
        configurationService.setConfigurableService(configurableService3);
        configurationService.setConfigurableService(configurableService4);
        configurationService.setFileOperator(fileOperator);
        configurationService.registerRepositoryHandler(repositoryHandler);

        Date date = new Date(0);
        Tag tag = Mockito.mock(Tag.class);
        Mockito.when(tag.getCreationDate()).thenReturn(date);
        Mockito.when(tag.getName()).thenReturn("testTag");
        Mockito.when(tag.getTagPath()).thenReturn((new File("testTagPath")).toPath());

        configurationService.notifyAboutNewTag(tag);
        
        Mockito.verify(configurableService1).reconfigure();
        Mockito.verify(configurableService2, Mockito.never()).reconfigure();
        Mockito.verify(configurableService3).reconfigure();
        Mockito.verify(configurableService4).reconfigure();
        Mockito.verify(remoteService1).stop();
        Mockito.verify(remoteService2).stop();
        Mockito.verify(remoteService1).start();
        Mockito.verify(remoteService2).start();
        Mockito.verify(repositoryHandler, Mockito.never()).getPreviousTag(tag);
    }
    
    @Test
    public void testNewTagReconfigurationFailed() {
        Date date = new Date(0);
        Tag tag = Mockito.mock(Tag.class);
        Mockito.when(tag.getCreationDate()).thenReturn(date);
        Mockito.when(tag.getName()).thenReturn("testTag");
        Mockito.when(tag.getTagPath()).thenReturn((new File("testTagPath")).toPath());
        
        Tag previousTag = Mockito.mock(Tag.class);
        Mockito.when(previousTag.getCreationDate()).thenReturn(date);
        Mockito.when(previousTag.getName()).thenReturn("previousTag");
        Mockito.when(previousTag.getTagPath()).thenReturn((new File("previousTagPath")).toPath());
        
        BundleContext bundleContext = Mockito.mock(BundleContext.class);

        RepositoryHandler repositoryHandler = Mockito.mock(RepositoryHandler.class);
        Mockito.when(repositoryHandler.getPreviousTag(Mockito.any(Tag.class))).thenReturn(previousTag);

        ConfigurableService configurableService1 = Mockito.mock(ConfigurableService.class);
        Mockito.when(configurableService1.reconfigure()).thenReturn(true);
        
        List<String> configurableService1Configs = new ArrayList<>();
        configurableService1Configs.add(configurationPath + "./folder1/config2.txt");
        Mockito.when(configurableService1.getPropertyList()).thenReturn(configurableService1Configs);

        ConfigurableService configurableService2 = Mockito.mock(ConfigurableService.class);
        Mockito.when(configurableService2.reconfigure()).thenReturn(true);
        
        List<String> configurableService2Configs = new ArrayList<>();
        configurableService2Configs.add(configurationPath + "./config3.txt");
        Mockito.when(configurableService2.getPropertyList()).thenReturn(configurableService2Configs);
        
        ConfigurableService configurableService3 = Mockito.mock(ConfigurableService.class);
        Mockito.when(configurableService3.reconfigure()).thenReturn(false).thenReturn(true);
        
        List<String> configurableService3Configs = new ArrayList<>();
        configurableService3Configs.add(configurationPath + "./folder1");
        Mockito.when(configurableService3.getPropertyList()).thenReturn(configurableService3Configs);
        
        ConfigurableService configurableService4 = Mockito.mock(ConfigurableService.class);
        Mockito.when(configurableService4.reconfigure()).thenReturn(true);
        
        List<String> configurableService4Configs = new ArrayList<>();
        configurableService4Configs.add(configurationPath + "./config1.txt");
        Mockito.when(configurableService4.getPropertyList()).thenReturn(configurableService4Configs);

        RemoteService remoteService1 = Mockito.mock(RemoteService.class);
        Mockito.when(remoteService1.stop()).thenReturn(true);
        Mockito.when(remoteService1.start()).thenReturn(true);

        RemoteService remoteService2 = Mockito.mock(RemoteService.class);
        Mockito.when(remoteService2.stop()).thenReturn(true);
        Mockito.when(remoteService2.start()).thenReturn(true);
        
        FileOperator fileOperator = Mockito.mock(FileOperator.class);
        
        List<String> changedConfigs = new ArrayList<>();
        changedConfigs.add(configurationPath + "./config1.txt");
        changedConfigs.add(configurationPath + "./folder1/config2.txt");
        Mockito.when(fileOperator.compareFolders(Mockito.any(File.class), Mockito.any(File.class)))
                .thenReturn(changedConfigs);
        Mockito.when(fileOperator.listFiles(new File(configurationPath))).thenReturn(new File[0]);

        VFSConfigurationService configurationService = new VFSConfigurationService(bundleContext);
        configurationService.start();
        configurationService.registerRemoteService(remoteService1);
        configurationService.registerRemoteService(remoteService2);
        configurationService.setConfigurableService(configurableService1);
        configurationService.setConfigurableService(configurableService2);
        configurationService.setConfigurableService(configurableService3);
        configurationService.setConfigurableService(configurableService4);
        configurationService.setFileOperator(fileOperator);
        configurationService.registerRepositoryHandler(repositoryHandler);

        configurationService.notifyAboutNewTag(previousTag);
        
        Mockito.verify(configurableService1, Mockito.times(2)).reconfigure();
        Mockito.verify(configurableService2, Mockito.never()).reconfigure();
        Mockito.verify(configurableService3, Mockito.times(2)).reconfigure();
        Mockito.verify(configurableService4, Mockito.times(1)).reconfigure();
        Mockito.verify(remoteService1, Mockito.atLeast(1)).stop();
        Mockito.verify(remoteService2, Mockito.atLeast(1)).stop();
        Mockito.verify(remoteService1).start();
        Mockito.verify(remoteService2).start();
        //check for any tag because it is not possible to check for a mocked parameter
        Mockito.verify(repositoryHandler).getPreviousTag(Mockito.any(Tag.class));
    }
}
