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

package org.openengsb.core.services.internal.deployer.context;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code ArtifactInstaller} that deploys from .context files
 */
public class ContextDeployerService extends AbstractOpenEngSBService implements ArtifactInstaller {

    private static final String CONTEXT_EXTENSION = ".context";

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextDeployerService.class);

    private ContextCurrentService contextCurrentService;

    @Override
    public boolean canHandle(File artifact) {
        LOGGER.debug("ContextDeployerService.canHandle(\"{}\")", artifact.getAbsolutePath());

        if (artifact.isFile() && artifact.getName().endsWith(CONTEXT_EXTENSION)) {
            LOGGER.info("Found a .context file to deploy.");
            return true;
        }
        return false;
    }

    @Override
    public void install(File artifact) throws Exception {
        LOGGER.debug("Trying to install context file \"{}\"", artifact.getName());
        String contextId = FilenameUtils.removeExtension(artifact.getName());
        if (!contextCurrentService.getAvailableContexts().contains(contextId)) {
            contextCurrentService.createContext(contextId);
        }
        LOGGER.info("Successfully installed context file \"{}\"", artifact.getName());
    }

    @Override
    public void update(File artifact) throws Exception {
        LOGGER.debug("Trying to update context file \"{}\"", artifact.getName());
        install(artifact);
    }

    @Override
    public void uninstall(File artifact) throws Exception {
        LOGGER.debug("Trying to uninstall context file \"{}\"", artifact.getName());
        throw new UnsupportedOperationException("Method not implemented by now");
    }

    public void setContextCurrentService(ContextCurrentService contextCurrentService) {
        this.contextCurrentService = contextCurrentService;
    }

}
