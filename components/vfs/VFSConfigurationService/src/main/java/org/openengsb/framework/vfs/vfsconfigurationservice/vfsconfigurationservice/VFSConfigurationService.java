/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.framework.vfs.vfsconfigurationservice.vfsconfigurationservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.openengsb.framework.vfs.api.common.Tag;
import org.openengsb.framework.vfs.api.configurableservice.ConfigurableService;
import org.openengsb.framework.vfs.api.configurationservice.ConfigurationService;
import org.openengsb.framework.vfs.api.exceptions.ReconfigurationException;
import org.openengsb.framework.vfs.api.exceptions.RemoteServiceException;
import org.openengsb.framework.vfs.api.remoteservice.RemoteService;
import org.openengsb.framework.vfs.api.repositoryhandler.RepositoryHandler;
import org.openengsb.framework.vfs.vfsconfigurationservice.fileoperations.ConfigurationFileManipulator;
import org.openengsb.framework.vfs.vfsconfigurationservice.fileoperations.VFSConfigurationFileManipulator;
import org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener.ConfigurableServiceListener;
import org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener.RemoteServiceListener;
import org.openengsb.framework.vfs.vfsconfigurationservice.servicelistener.RepositoryHandlerListener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VFSConfigurationService implements ConfigurationService {

    private final Logger logger = LoggerFactory.getLogger(VFSConfigurationService.class);
    private RepositoryHandler repositoryHandler;
    private ResourceBundle configurationServiceProperties = ResourceBundle.getBundle("configurationservice");
    private ArrayList<RemoteService> remoteServices = new ArrayList<>();
    private ArrayList<ConfigurableService> configurableServices = new ArrayList<>();
    private RemoteServiceListener remoteServiceListener = null;
    private BundleContext bundleContext = null;
    private ConfigurableServiceListener configurableServiceListener = null;
    private RepositoryHandlerListener repositoryHandlerListener = null;
    private ConfigurationFileManipulator fileOperator = new VFSConfigurationFileManipulator();
    private List<ConfigurableService> servicesToReconfigure = new ArrayList<>();
    private File tempFolder = null;
    private String configurationFolderPath = configurationServiceProperties.getString("configurationPath");
    private File configFolder = new File(configurationFolderPath);

    public VFSConfigurationService(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setFileOperator(ConfigurationFileManipulator fileOperator) {
        this.fileOperator = fileOperator;
    }

    @Override
    public void notifyAboutNewTag(Tag tag) {
        logger.debug("New tag arrived, reconfigure OpenEngSB");

        if (repositoryHandler == null) {
            logger.debug("Could not set new Tag");
            logger.debug("RspositoryHanlder not set yet, start bundle first");
            return;
        }

        //Stop all remoteServices
        for (RemoteService remote : remoteServices) {
            try {
                remote.stop();
            }
            catch (RemoteServiceException ex) {
                logger.debug("Could not set new Tag");
                logger.debug("problem stopping remoteService, new Tag will not be deployed");
                logger.debug("Exception Message: " + ex.getMessage());
                return;
            }
        }

        if (!configFolder.exists()) {
            try {
                fileOperator.createDirectories(configFolder.toPath());
            }
            catch (IOException ex) {
                logger.debug("error creating configuration directory: " + ex.getMessage());
                return;
            }
        }

        if (tempFolder == null) {
            UUID uuid = UUID.randomUUID();
            String tempFolderName = "vfstemp_" + uuid.toString();
            tempFolder = new File(tempFolderName);
            if (!tempFolder.exists() || !tempFolder.isDirectory()) {
                try {
                    fileOperator.createDirectories(tempFolder.toPath());
                }
                catch (IOException ex) {
                    logger.debug("error creating temp file");
                }
            }
            try {
                fileOperator.copy(configFolder.toPath(), tempFolder.toPath());
            }
            catch (IOException ex) {
                logger.debug("error copying configuration to temp directory" + ex.getMessage());
            }
        }

        for (File f : fileOperator.listFiles(configFolder)) {
            fileOperator.fileDelete(f);
        }
        try {
            fileOperator.copy(tag.getTagPath(), configFolder.toPath());
        }
        catch (IOException ex) {
            logger.debug("error deleting configuration" + ex.getMessage());
        }

        List<String> changes = fileOperator.compareFolders(tempFolder, configFolder);

        for (ConfigurableService service : configurableServices) {
            for (String s : service.getPropertyList()) {
                String serviceProperty = s.replace("\\", "/");
                for (String c : changes) {
                    String change = c.replace("\\", "/");
                    if (change.trim().startsWith(serviceProperty.trim())) {
                        if (!servicesToReconfigure.contains(service)) {
                            servicesToReconfigure.add(service);
                        }
                        break;
                    }
                }
            }
        }

        for (ConfigurableService service : servicesToReconfigure) {
            try {
                //reconfigure Service and check if reconfigure was successful
                service.reconfigure();
            }
            catch (ReconfigurationException ex) {
                logger.debug("Exception Message: " + ex.getMessage());
                loadPreviousTag(tag);
                //When the previous config was start successful return and end it. 
                return;
            }
        }

        //Restart all remoteServices
        for (RemoteService remote : remoteServices) {
            try {
                remote.start();
            }
            catch (RemoteServiceException ex) {
                logger.debug("error restart remoteService -- reload previous config");
                logger.debug("Exception Message: " + ex.getMessage());
                loadPreviousTag(tag);
                //When the previous config was start successful return and end it. 
                return;
            }
        }

        servicesToReconfigure.clear();

        try {
            FileUtils.deleteDirectory(tempFolder);
            tempFolder = null;
        }
        catch (IOException ex) {
            logger.error("error deleting temp directory " + ex.getMessage());
        }

        logger.debug("Successfully extracted Tag to configurationfolder");
    }

    //Load the previous configuration and set the configuration
    private void loadPreviousTag(Tag actualTag) {
        logger.debug("Error starting openEngSB woth the new Configuration");
        logger.debug("Geting old config and try again");
        Tag previousTag = repositoryHandler.getPreviousTag(actualTag);
        if (previousTag != null) {
            notifyAboutNewTag(previousTag);
        }
        else {
            for (File f : fileOperator.listFiles(configFolder)) {
                fileOperator.fileDelete(f);
            }
            try {
                fileOperator.copy(tempFolder.toPath(), configFolder.toPath());
            }
            catch (IOException ex) {
                logger.error("could not revert config" + ex.getMessage());
            }

            for (ConfigurableService service : servicesToReconfigure) {
                try {
                    service.reconfigure();
                }
                catch (ReconfigurationException ex) {
                    logger.error("could not revert to the old configuration. Message. " + ex.getMessage());
                }
            }

            for (RemoteService remote : remoteServices) {
                try {
                    remote.start();
                }
                catch (RemoteServiceException ex) {
                    logger.debug("could not restart remote service after reverting to old configuration. Message: " + ex.getMessage());
                }
            }

            try {
                FileUtils.deleteDirectory(tempFolder);
                tempFolder = null;
            }
            catch (IOException ex) {
                logger.error("error deleting temp directory " + ex.getMessage());
            }
        }
    }

    public void start() {
        configurableServiceListener = new ConfigurableServiceListener(bundleContext, this);
        configurableServiceListener.open();
        remoteServiceListener = new RemoteServiceListener(bundleContext, this);
        remoteServiceListener.open();
        repositoryHandlerListener = new RepositoryHandlerListener(bundleContext, this);
        repositoryHandlerListener.open();
    }

    public void stop() {
        configurableServiceListener.close();
        remoteServiceListener.close();
        repositoryHandlerListener.close();
    }

    public void registerRepositoryHandler(RepositoryHandler repositoryhandler) {
        this.repositoryHandler = repositoryhandler;
    }

    public void unregisterRepositoryHandler() {
        this.repositoryHandler = null;
    }

    public void registerRemoteService(RemoteService remoteService) {
        if (!this.remoteServices.contains(remoteService)) {
            remoteServices.add(remoteService);
        }
    }

    public void unregisterRemoteService(RemoteService remoteService) {
        if (this.remoteServices.contains(remoteService)) {
            remoteServices.remove(remoteService);
        }
    }

    public void setConfigurableService(ConfigurableService configurableService) {
        if (!configurableServices.contains(configurableService)) {
            configurableServices.add(configurableService);
        }
    }

    public void setConfigurableServiceLost(ConfigurableService configurableService) {
        if (configurableServices.contains(configurableService)) {
            configurableServices.remove(configurableService);
        }
    }
}
