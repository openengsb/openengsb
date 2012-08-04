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
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.util.ConfigUtils;
import org.openengsb.core.common.util.MergeException;
import org.openengsb.core.security.SecurityContext;
import org.openengsb.core.services.internal.deployer.connector.ConnectorFile.ChangeSet;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Sets;

public class ConnectorDeployerService extends AbstractOpenEngSBService
        implements ArtifactInstaller {

    private static final String CONNECTOR_EXTENSION = ".connector";

    private static final String DOMAIN_PATTERN = "(" + org.openengsb.core.api.Constants.DOMAIN_KEY + "=%s)";

    private static final String DOMAIN_CONNECTOR_PATTERN = "("
            + "&(" + org.openengsb.core.api.Constants.DOMAIN_KEY + "=%s)"
            + "(" + org.openengsb.core.api.Constants.CONNECTOR_KEY + "=%s)"
            + ")";

    private static final String DOMAIN_PROVIDER_PATTERN =
            "(" + Constants.OBJECTCLASS + "=" + DomainProvider.class.getName() + ")";

    private static final String CONNECTOR_FACTORY_PATTERN =
            "(" + Constants.OBJECTCLASS + "=" + ConnectorInstanceFactory.class.getName() + ")";

    private static final Logger LOGGER = LoggerFactory
        .getLogger(ConnectorDeployerService.class);

    private ConnectorManager serviceManager;
    private LoadingCache<File, ConnectorFile> oldConfigs = CacheBuilder.newBuilder().build(
        new CacheLoader<File, ConnectorFile>() {
            @Override
            public ConnectorFile load(File key) throws Exception {
                return new ConnectorFile(key);
            }
        });

    private LoadingCache<File, Semaphore> updateSemaphores = CacheBuilder.newBuilder().build(
        new CacheLoader<File, Semaphore>() {
            @Override
            public Semaphore load(File key) throws Exception {
                return new Semaphore(1);
            }
        });

    private Set<File> failedInstalls = Sets.newHashSet();

    private BundleContext bundleContext;

    public void init() throws InvalidSyntaxException {
        bundleContext.addServiceListener(new ServiceListener() {
            @Override
            public void serviceChanged(ServiceEvent event) {
                try{
                tryInstallFailed();
                } catch(Exception e){
                    LOGGER.debug("exception while trying to install connectors after new found domain or connector", e);
                }
            }
        }, "(|" + DOMAIN_PROVIDER_PATTERN + CONNECTOR_FACTORY_PATTERN + ")");
    }

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
        synchronized (failedInstalls) {
            if (!doInstall(artifact)) {
                failedInstalls.add(artifact);
            }
        }
    }

    private boolean doInstall(File artifact) {
        final ConnectorFile configFile;
        try {
            configFile = oldConfigs.get(artifact);
        } catch (ExecutionException e) {
            LOGGER.error("severe error when installing artifact", e);
            return false;
        }
        configFile.update(artifact);
        final Map<String, Object> properties = new Hashtable<String, Object>(
                configFile.getProperties());

        if (properties.get(Constants.SERVICE_RANKING) == null
                && ConnectorFile.isRootService(artifact)) {
            properties.put(Constants.SERVICE_RANKING, -1);
        }
        LOGGER.info("Loading instance {}", configFile.getName());

        final String name = FilenameUtils.removeExtension(artifact.getName());

        final Map<String, String> attributes = configFile.getAttributes();

        if (!haveDomainProvider(configFile.getDomainType())) {
            LOGGER.info("installing {} delayed. Waiting for DomainProvider", artifact);
            return false;
        }
        if (!haveConnectorFactory(configFile.getDomainType(), configFile.getConnectorType())) {
            LOGGER.info("installing {} delayed. Waiting for ConnectorFactory", artifact);
            return false;
        }
        try {
            SecurityContext.executeWithSystemPermissions(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    ConnectorDescription connectorDescription =
                            new ConnectorDescription(configFile.getDomainType(), configFile.getConnectorType(),
                                    attributes, properties);
                    try{
                    serviceManager.createWithId(name, connectorDescription);
                    }catch (IllegalArgumentException e){
                        if(e.getMessage().contains("connector already exists")){
                            return null;
                        }
                        throw e;
                    }
                    return null;
                }
            });
        } catch (ExecutionException e) {
            LOGGER.info("installing {} delayed", artifact);
            LOGGER.debug("installing {} failed because of Exception", artifact, e);
            return false;
        }
        return true;
    }

    public void tryInstallFailed() {
        synchronized (failedInstalls) {
            Iterator<File> failedInstallsIterator = failedInstalls.iterator();
            while (failedInstallsIterator.hasNext()) {
                File fileToInstall = failedInstallsIterator.next();
                if (doInstall(fileToInstall)) {
                    failedInstallsIterator.remove();
                }
            }
        }
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
        final String connectorId = connectorFile.getName();
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

        SecurityContext.executeWithSystemPermissions(new Callable<Object>() {
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
        return new ConnectorDescription(changes.getDomainType(), changes.getConnectorType(), newAttributes,
            new Hashtable<String, Object>(newProperties));
    }

    @Override
    public void uninstall(final File artifact) throws Exception {
        LOGGER.debug("ConnectorDeployer.uninstall(\"{}\")",
            artifact.getAbsolutePath());
        SecurityContext.executeWithSystemPermissions(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                String name = FilenameUtils.removeExtension(artifact.getName());
                serviceManager.delete(name);
                return null;
            }
        });
    }

    protected boolean haveConnectorFactory(String domainType, String connectorType) {
        String connectorFilter = String.format(DOMAIN_CONNECTOR_PATTERN, domainType, connectorType);
        try {
            Collection<ServiceReference<ConnectorInstanceFactory>> serviceReferences =
                    bundleContext.getServiceReferences(ConnectorInstanceFactory.class, connectorFilter);
            return !serviceReferences.isEmpty();
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean haveDomainProvider(String domain) {
        String domainFilter = String.format(DOMAIN_PATTERN, domain);
        try {
            Collection<ServiceReference<DomainProvider>> serviceReferences =
                    bundleContext.getServiceReferences(DomainProvider.class, domainFilter);
            return !serviceReferences.isEmpty();
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void setServiceManager(ConnectorManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
