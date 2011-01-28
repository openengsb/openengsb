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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.util.OsgiServiceUtils;
import org.osgi.framework.BundleContext;

public class ConnectorDeployer implements ArtifactInstaller {

    private static final String PROPERTY_CONNECTOR = "connector";

    private static Log log = LogFactory.getLog(ConnectorDeployer.class);

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

        String connectorName = getConnectorNameFrom(artifact);
        log.info(String.format("Load instance of %s", connectorName));
    
        ServiceManager serviceManager = OsgiServiceUtils.getService(bundleContext, ServiceManager.class, String.format("(connector=%s)", connectorName));
    }

    @Override
    public void update(File artifact) throws Exception {
        log.debug(String.format("ConnectorDeployer.update(\"%s\")", artifact.getAbsolutePath()));
        // TODO Auto-generated method stub

    }

    @Override
    public void uninstall(File artifact) throws Exception {
        log.debug(String.format("ConnectorDeployer.uninstall(\"%s\")", artifact.getAbsolutePath()));
        // TODO Auto-generated method stub

    }

    private String getConnectorNameFrom(File artifact) throws IOException, FileNotFoundException {
        Properties props = new Properties();
        props.load(new FileInputStream(artifact.getAbsoluteFile()));
        String connectorName = props.getProperty(PROPERTY_CONNECTOR);
        return connectorName;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

}
