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
package org.openengsb.core.ekb.internal.deployer;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.api.ekb.TransformationEngine;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.transformations.TransformationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * {@code ArtifactInstaller} that deploys transformation description files
 */
public class TransformationDeployerService extends AbstractOpenEngSBService implements ArtifactInstaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationDeployerService.class);
    private static final String TRANSFORM_ENDING = "transformation";
    private TransformationEngine engine;

    @Override
    public boolean canHandle(File artifact) {
        LOGGER.debug("TransformationDeployer.canHandle(\"{}\")", artifact.getAbsolutePath());
        String fileEnding = FilenameUtils.getExtension(artifact.getName());

        if (artifact.isFile() && TRANSFORM_ENDING.equals(fileEnding)) {
            LOGGER.info("Found \"{}\" to deploy.", artifact);
            return true;
        }
        return false;
    }

    @Override
    public void install(File artifact) throws Exception {
        LOGGER.debug("TransformationDeployer.install(\"{}\")", artifact.getAbsolutePath());
        try {
            doInstall(artifact);
            LOGGER.info("Successfully installed transformation file \"{}\"", artifact.getName());
        } catch (Exception e) {
            LOGGER.error("Error while installing transformation file", e);
            throw e;
        }
    }

    private void doInstall(File artifact) throws RuleBaseException, IOException, SAXException,
        ParserConfigurationException {
        try {
            LOGGER.info("Read the transformation description file " + artifact.getName());
            engine.saveDescriptions(TransformationUtils.getDescriptionsFromXMLFile(artifact));
        } catch (Exception e) {
            LOGGER.error("Error while reading the content of file " + artifact.getName(), e);
        }
    }

    @Override
    public void update(File artifact) throws Exception {
        LOGGER.debug("TransformationDeployer.update(\"{}\")", artifact.getAbsolutePath());
        try {
            doInstall(artifact);
        } catch (Exception e) {
            LOGGER.error("Error while updating transformation file", e);
            throw e;
        }
        LOGGER.info("Successfully updated transformation file \"{}\"", artifact.getName());
    }

    @Override
    public void uninstall(File artifact) throws Exception {
        LOGGER.debug("TransformationDeployer.uninstall(\"{}\")", artifact.getAbsolutePath());
        try {
            engine.deleteDescriptionsByFile(artifact.getName());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        LOGGER.info("Successfully deleted transformation file \"{}\"", artifact.getName());
    }

    public void setEngine(TransformationEngine engine) {
        this.engine = engine;
    }
}
