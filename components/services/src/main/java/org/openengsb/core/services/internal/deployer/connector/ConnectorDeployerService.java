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

package org.openengsb.core.services.internal.deployer.connector;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.model.ConnectorDefinition;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.util.ConfigUtils;
import org.openengsb.core.common.util.MergeException;
import org.openengsb.core.common.util.SecurityUtils;
import org.openengsb.core.services.internal.deployer.connector.ConnectorFile.ChangeSet;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.MapDifference;

public class ConnectorDeployerService extends AbstractOpenEngSBService
        implements ArtifactInstaller {

    private static final String CONNECTOR_EXTENSION = ".connector";

    private static final Logger LOGGER = LoggerFactory
        .getLogger(ConnectorDeployerService.class);

    private ConnectorManager serviceManager;
    private Cache<File, ConnectorFile> oldConfigs = CacheBuilder.newBuilder().build(
        new CacheLoader<File, ConnectorFile>() {
            @Override
            public ConnectorFile load(File key) throws Exception {
                return new ConnectorFile(key);
            }
        });

    private Cache<File, Semaphore> updateSemaphores = CacheBuilder.newBuilder().build(
        new CacheLoader<File, Semaphore>() {
            @Override
            public Semaphore load(File key) throws Exception {
                return new Semaphore(1);
            }
        });

    @Override
    public boolean canHandle(File artifact) {
        LOGGER.debug("ConnectorDeployer.canHandle(\"{}\")",
            artifact.getAbsolutePath());
        if (artifact.isFile()
                && artifact.getName().endsWith(CONNECTOR_EXTENSION)) {
            LOGGER.info("Found a .connector file to deploy.");
            return true;
        }
        return false;
    }

    @Override
    public void install(File artifact) throws Exception {
        LOGGER.debug("ConnectorDeployer.install(\"{}\")",
            artifact.getAbsolutePath());
        final ConnectorFile configFile = oldConfigs.get(artifact);
        configFile.update(artifact);
        final Map<String, Object> properties = new Hashtable<String, Object>(
            configFile.getProperties());

        if (properties.get(Constants.SERVICE_RANKING) == null
                && ConnectorFile.isRootService(artifact)) {
            properties.put(Constants.SERVICE_RANKING, -1);
        }
        LOGGER.info("Loading instance {}", configFile.getConnectorId());

        SecurityUtils.executeWithSystemPermissions(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                serviceManager.create(configFile.getConnectorId(),
                    new ConnectorDescription(new HashMap<String, String>(
                        configFile.getAttributes()), properties));
                return null;
            }
        });

    }

    @Override
    public void update(File artifact) throws Exception {
        LOGGER.debug("ConnectorDeployer.update(\"{}\")",
            artifact.getAbsolutePath());
        Semaphore semaphore = updateSemaphores.get(artifact);
        semaphore.acquire();
        try {
            doUpdate(artifact);
        } finally {
            semaphore.release();
        }
    }

    private void doUpdate(File artifact) throws Exception {
        ConnectorFile connectorFile = oldConfigs.get(artifact);
        final ConnectorDefinition connectorId = connectorFile.getConnectorId();
        ConnectorDescription persistenceContent = serviceManager
            .getAttributeValues(connectorId);
        ChangeSet changes = connectorFile.getChanges(artifact);

        final ConnectorDescription newDescription;
        try {
            newDescription = applyChanges(persistenceContent, changes);
            connectorFile.update(artifact);
        } catch (MergeException e) {
            File backupFile = getBackupFile(artifact);
            FileUtils.moveFile(artifact, backupFile);
            Properties properties = connectorFile.toProperties();
            properties.store(new FileWriter(artifact),
                "Connector update failed. The invalid connector-file has been saved to "
                        + backupFile.getName());
            throw e;
        }

        SecurityUtils.executeWithSystemPermissions(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                serviceManager.update(connectorId, newDescription);
                return null;
            }
        });

    }

    private File getBackupFile(File artifact) {
        int backupNumber = 0;
        String candidate = artifact.getAbsolutePath();
        File candFile = new File(candidate);
        while (candFile.exists()) {
            backupNumber++;
            String suffix = StringUtils.leftPad(Integer.toString(backupNumber),
                3, "0");
            candidate = artifact.getAbsolutePath() + "_" + suffix;
            candFile = new File(candidate);
        }
        return candFile;
    }

    private ConnectorDescription applyChanges(ConnectorDescription persistenceContent, ChangeSet changes)
        throws MergeException {
        MapDifference<String, String> changedAttributes = changes
            .getChangedAttributes();
        Map<String, String> attributes = persistenceContent.getAttributes();

        Map<String, String> newAttributes = ConfigUtils.updateMap(attributes,
            changedAttributes);
        Map<String, Object> newProperties = ConfigUtils.updateMap(
            persistenceContent.getProperties(),
            changes.getChangedProperties());
        return new ConnectorDescription(newAttributes,
            new Hashtable<String, Object>(newProperties));
    }

    @Override
    public void uninstall(final File artifact) throws Exception {
        LOGGER.debug("ConnectorDeployer.uninstall(\"{}\")",
            artifact.getAbsolutePath());
        SecurityUtils.executeWithSystemPermissions(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                String name = FilenameUtils.removeExtension(artifact.getName());
                ConnectorDefinition fullId = ConnectorDefinition.fromFullId(name);
                serviceManager.delete(fullId);
                return null;
            }
        });
    }

    public void setServiceManager(ConnectorManager serviceManager) {
        this.serviceManager = serviceManager;
    }

}
