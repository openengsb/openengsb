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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.common.proxy.ProxyFactory;
import org.openengsb.core.common.service.DomainService;
import org.osgi.framework.BundleContext;

public class ConnectorDeployer implements ArtifactInstaller {

    private static Log log = LogFactory.getLog(ConnectorDeployer.class);

    private DomainService domainService;
    private ProxyFactory proxyFactory;
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
        // TODO Auto-generated method stub
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

    public void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public ProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public void setDomainService(DomainService domainService) {
        this.domainService = domainService;
    }

    public DomainService getDomainService() {
        return domainService;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

}
