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
import java.util.concurrent.Semaphore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.util.ConfigUtils;
import org.openengsb.core.common.util.MergeException;
import org.openengsb.core.security.BundleAuthenticationToken;
import org.openengsb.core.services.internal.deployer.connector.ConnectorFile.ChangeSet;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.base.Function;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapMaker;

public class ConnectorDeployerService extends AbstractOpenEngSBService implements ArtifactInstaller {

    private static final String CONNECTOR_EXTENSION = ".connector";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorDeployerService.class);

    private AuthenticationManager authenticationManager;
    private ConnectorManager serviceManager;
    private Map<File, ConnectorFile> oldConfigs = new MapMaker().makeComputingMap(new Function<File, ConnectorFile>() {
        @Override
        public ConnectorFile apply(File input) {
            return new ConnectorFile(input);
        }
    });

    private Map<File, Semaphore> updateSemaphores = new MapMaker()
        .makeComputingMap(new Function<File, Semaphore>() {
            @Override
            public Semaphore apply(File input) {
                return new Semaphore(1);
            };
        });

    @Override
    public boolean canHandle(File artifact) {
        LOGGER.debug("ConnectorDeployer.canHandle(\"{}\")", artifact.getAbsolutePath());
        if (artifact.isFile() && artifact.getName().endsWith(CONNECTOR_EXTENSION)) {
            LOGGER.info("Found a .connector file to deploy.");
            return true;
        }
        return false;
    }

    @Override
    public void install(File artifact) throws Exception {
        LOGGER.debug("ConnectorDeployer.install(\"{}\")", artifact.getAbsolutePath());
        ConnectorFile configFile = oldConfigs.get(artifact);

        Map<String, Object> properties = new Hashtable<String, Object>(configFile.getProperties());

        if (properties.get(Constants.SERVICE_RANKING) == null && ConnectorFile.isRootService(artifact)) {
            properties.put(Constants.SERVICE_RANKING, "-1");
        }
        LOGGER.info("Loading instance {}", configFile.getConnectorId());

        login();
        try {
            serviceManager.create(configFile.getConnectorId(),
                new ConnectorDescription(new HashMap<String, String>(configFile.getAttributes()), properties));
        } finally {
            logout();
        }
    }

    @Override
    public void update(File artifact) throws Exception {
        LOGGER.debug("ConnectorDeployer.update(\"{}\")", artifact.getAbsolutePath());
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
        ConnectorId connectorId = connectorFile.getConnectorId();
        ConnectorDescription persistenceContent = serviceManager.getAttributeValues(connectorId);
        ChangeSet changes = connectorFile.getChanges(artifact);

        ConnectorDescription newDescription;
        try {
            newDescription = applyChanges(persistenceContent, changes);
            connectorFile.update(artifact);
        } catch (MergeException e) {
            File backupFile = getBackupFile(artifact);
            FileUtils.moveFile(artifact, backupFile);
            Properties properties = connectorFile.toProperties();
            properties.store(new FileWriter(artifact),
                "Connector update failed. The invalid connector-file has been saved to " + backupFile.getName());
            throw e;
        }

        login();
        try {
            serviceManager.update(connectorId, newDescription);
        } finally {
            logout();
        }
    }

    private File getBackupFile(File artifact) {
        int backupNumber = 0;
        String candidate = artifact.getAbsolutePath();
        File candFile = new File(candidate);
        while (candFile.exists()) {
            backupNumber++;
            String suffix = StringUtils.leftPad(Integer.toString(backupNumber), 3, "0");
            candidate = artifact.getAbsolutePath() + "_" + suffix;
            candFile = new File(candidate);
        }
        return candFile;
    }

    private ConnectorDescription applyChanges(ConnectorDescription persistenceContent, ChangeSet changes)
        throws MergeException {
        MapDifference<String, String> changedAttributes = changes.getChangedAttributes();
        Map<String, String> attributes = persistenceContent.getAttributes();

        Map<String, String> newAttributes = ConfigUtils.updateMap(attributes, changedAttributes);
        Map<String, Object> newProperties =
            ConfigUtils.updateMap(persistenceContent.getProperties(), changes.getChangedProperties());
        return new ConnectorDescription(newAttributes, new Hashtable<String, Object>(newProperties));
    }

    @Override
    public void uninstall(File artifact) throws PersistenceException {
        LOGGER.debug("ConnectorDeployer.uninstall(\"{}\")", artifact.getAbsolutePath());
        login();
        try {
            String name = FilenameUtils.removeExtension(artifact.getName());
            ConnectorId fullId = ConnectorId.fromFullId(name);
            serviceManager.delete(fullId);
        } finally {
            logout();
        }
    }

    private void login() {
        Authentication authentication =
            authenticationManager.authenticate(new BundleAuthenticationToken("core-services", ""));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void logout() {
        SecurityContextHolder.clearContext();
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setServiceManager(ConnectorManager serviceManager) {
        this.serviceManager = serviceManager;
    }

}
