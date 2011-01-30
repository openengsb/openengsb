/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.deployer.connector;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.osgi.framework.BundleContext;

public class ConnectorDeployerService extends AbstractOpenEngSBService implements ArtifactInstaller {

    private static Log log = LogFactory.getLog(ConnectorDeployerService.class);

    private BundleContext bundleContext;

    private static final String CONNECTOR_EXTENSION = ".connector";

    @Override
    public boolean canHandle(File artifact) {
        log.debug(String.format("ConnectorDeployer.canHandle(\"%s\")", artifact.getAbsolutePath()));

        if (artifact.isFile() && artifact.getName().endsWith(CONNECTOR_EXTENSION)) {
            log.info("Found a .connector file to deploy.");
            return true;
        }

        return false;
    }

    @Override
    public void install(File artifact) throws Exception {
        log.debug(String.format("ConnectorDeployer.install(\"%s\")", artifact.getAbsolutePath()));

        ConnectorConfiguration newConfig = ConnectorConfiguration.loadFromFile(new ConnectorFile(artifact));

        if (!isConfigValid(newConfig)) {
            logConfigErrors(newConfig, artifact);
            return;
        }

        log.info(String.format("Loading instance %s of connector %s", newConfig.getServiceId(),
                newConfig.getConnectorType()));
        ServiceManager serviceManager = OsgiServiceUtils.getService(bundleContext, ServiceManager.class,
                String.format("(connector=%s)", newConfig.getConnectorType()));
        if (serviceManager == null) {
            log.info(String.format(
                    "Retrieving ServiceManager for connector %s failed, cannot create connector instance",
                    newConfig.getConnectorType()));
            return;
        }

        Map<String, String> attributes = new HashMap<String, String>();
        MultipleAttributeValidationResult validationResult = serviceManager
                .update(newConfig.getServiceId(), attributes);
        log.info(String.format("Connector %s of type %s valid: %b", newConfig.getConnectorType(),
                newConfig.getServiceId(), validationResult.isValid()));
    }

    private void logConfigErrors(ConnectorConfiguration newConfig, File artifact) {
        if (newConfig.getConnectorType() == null) {
            log.info(String.format("Malformed configuration file %s: Connector name missing", artifact.getName()));
        }

        if (newConfig.getServiceId() == null) {
            log.info(String.format("Malformed configuration file %s: Service Id missing", artifact.getName()));
        }
    }

    private boolean isConfigValid(ConnectorConfiguration newConfig) {
        boolean hasConnectorType = newConfig.getConnectorType() != null;
        boolean hasServiceId = newConfig.getServiceId() != null;

        return hasConnectorType && hasServiceId;
    }

    @Override
    public void update(File artifact) throws Exception {
        log.debug(String.format("ConnectorDeployer.update(\"%s\")", artifact.getAbsolutePath()));
        install(artifact);
    }

    @Override
    public void uninstall(File artifact) throws Exception {
        log.debug(String.format("ConnectorDeployer.uninstall(\"%s\")", artifact.getAbsolutePath()));
        // TODO Auto-generated method stub

    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

}
