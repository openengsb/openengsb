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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.InternalServiceRegistrationManager;
import org.openengsb.core.api.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.osgi.framework.Constants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

public class ConnectorDeployerService extends AbstractOpenEngSBService implements ArtifactInstaller {

    private static final String AUTH_PASSWORD = "password";
    private static final String AUTH_USER = "admin";
    private static final String CONNECTOR_EXTENSION = ".connector";

    private static Log log = LogFactory.getLog(ConnectorDeployerService.class);

    private AuthenticationManager authenticationManager;
    private DeployerStorage deployerStorage;

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

        try {
            ConnectorConfiguration newConfig = ConnectorConfiguration.loadFromFile(new ConnectorFile(artifact));
            if (!isConfigValid(newConfig)) {
                logConfigErrors(newConfig, artifact);
                return;
            }
            authenticate(AUTH_USER, AUTH_PASSWORD);

            String serviceId = deployerStorage.getServiceId(artifact);
            if (serviceId == null) {
                serviceId = newConfig.getServiceId();
            }

            log.info(String.format("Loading instance %s of connector %s", serviceId, newConfig.getConnectorType()));
            InternalServiceRegistrationManager serviceManager = getServiceManagerFor(newConfig.getConnectorType());
            if (serviceManager == null) {
                log.info(String.format(
                        "Retrieving ServiceManager for connector %s failed, cannot create connector instance",
                        newConfig.getConnectorType()));
                return;
            }

            MultipleAttributeValidationResult validationResult = serviceManager.update(serviceId,
                    newConfig.getAttributes());
            if (validationResult.isValid()) {
                deployerStorage.put(artifact, newConfig);
            }
            log.info(String.format("Connector %s of type %s valid: %b", newConfig.getConnectorType(), serviceId,
                    validationResult.isValid()));
        } catch (Exception e) {
            log.error(String.format("Installing connector failed: %s", e));
            throw e;
        }
    }

    @Override
    public void update(File artifact) throws Exception {
        log.debug(String.format("ConnectorDeployer.update(\"%s\")", artifact.getAbsolutePath()));
        install(artifact);
    }

    @Override
    public void uninstall(File artifact) throws Exception {
        log.debug(String.format("ConnectorDeployer.uninstall(\"%s\")", artifact.getAbsolutePath()));

        try {
            authenticate(AUTH_USER, AUTH_PASSWORD);

            String serviceId = deployerStorage.getServiceId(artifact);
            if (serviceId == null) {
                return;
            }
            String connectorType = deployerStorage.getConnectorType(artifact);

            log.info(String.format("Removing instance %s of connector %s", serviceId, connectorType));
            InternalServiceRegistrationManager serviceManager = getServiceManagerFor(connectorType);
            if (serviceManager == null) {
                log.info(String.format(
                        "Retrieving ServiceManager for connector %s failed, cannot remove connector instance",
                        connectorType));
                return;
            }

            serviceManager.delete(serviceId);
            deployerStorage.remove(artifact);
        } catch (Exception e) {
            log.error(String.format("Removing connector failed: %s", e));
            throw e;
        }
    }

    private InternalServiceRegistrationManager getServiceManagerFor(String connectorType) throws OsgiServiceNotAvailableException {
        return (InternalServiceRegistrationManager) getOsgiUtils().getService(getFilterFor(connectorType));
    }

    private OsgiUtilsService getOsgiUtils() {
        return OpenEngSBCoreServices.getServiceUtilsService();
    }

    private String getFilterFor(String connectorType) {
        return String.format("(&(%s=%s)(connector=%s))", Constants.OBJECTCLASS, InternalServiceRegistrationManager.class.getName(),
            connectorType);
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

    private boolean authenticate(String username, String password) {
        boolean authenticated = false;
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            authenticated = authentication.isAuthenticated();
            log.info(String.format("Connector deployer succesfully authenticated: %b", authenticated));
        } catch (AuthenticationException e) {
            log.warn(String.format("User '%s' failed to login. Reason: %s", username, e.getMessage()));
            authenticated = false;
        }
        return authenticated;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setDeployerStorage(DeployerStorage deployerStorage) {
        this.deployerStorage = deployerStorage;
    }

}
