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
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

public class ConnectorDeployerService extends AbstractOpenEngSBService implements ArtifactInstaller {

    private static final String AUTH_PASSWORD = "password";
    private static final String AUTH_USER = "admin";
    private static final String CONNECTOR_EXTENSION = ".connector";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorDeployerService.class);

    private AuthenticationManager authenticationManager;
    private ConnectorManager serviceManager;

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

        ConnectorFile configFile = new ConnectorFile(artifact);
        ConnectorConfiguration newConfig = configFile.load();
        authenticate(AUTH_USER, AUTH_PASSWORD);
        if (newConfig.getContent().getProperties().get(Constants.SERVICE_RANKING) == null
                && ConnectorFile.isRootService(artifact)) {
            newConfig.getContent().getProperties().put(Constants.SERVICE_RANKING, "-1");
        }
        LOGGER.info("Loading instance {}", newConfig.getConnectorId());

        try {
            serviceManager.create(newConfig.getConnectorId(), newConfig.getContent());
        } catch (ConnectorValidationFailedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(File artifact) throws IOException {
        LOGGER.debug("ConnectorDeployer.update(\"{}\")", artifact.getAbsolutePath());
        ConnectorConfiguration newConfig = new ConnectorFile(artifact).load();
        authenticate(AUTH_USER, AUTH_PASSWORD);
        try {
            serviceManager.update(newConfig.getConnectorId(), newConfig.getContent());
        } catch (ConnectorValidationFailedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uninstall(File artifact) throws PersistenceException {
        LOGGER.debug("ConnectorDeployer.uninstall(\"{}\")", artifact.getAbsolutePath());
        authenticate(AUTH_USER, AUTH_PASSWORD);
        String name = FilenameUtils.removeExtension(artifact.getName());
        ConnectorId fullId = ConnectorId.fromFullId(name);
        serviceManager.delete(fullId);
    }

    private boolean authenticate(String username, String password) {
        boolean authenticated = false;
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            authenticated = authentication.isAuthenticated();
            LOGGER.info("Connector deployer succesfully authenticated: {}", authenticated);
        } catch (AuthenticationException e) {
            LOGGER.warn("User '{}' failed to login. Reason: ", username, e);
            authenticated = false;
        }
        return authenticated;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setServiceManager(ConnectorManager serviceManager) {
        this.serviceManager = serviceManager;
    }

}
