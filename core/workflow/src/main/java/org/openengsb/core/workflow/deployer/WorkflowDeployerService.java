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
package org.openengsb.core.workflow.deployer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * {@code ArtifactInstaller} that deploys workflow files
 */
public class WorkflowDeployerService extends AbstractOpenEngSBService implements ArtifactInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowDeployerService.class);
    private static final String RULE_ENDING = "rule";
    private static final String PROCESS_ENDING = "rf";
    private static final String FUNCTION_ENDING = "function";
    private static final String PACKAGE_ATTR = "package-name";
    private static Map<String, RuleBaseElementId> cache = new HashMap<String, RuleBaseElementId>();

    private RuleManager ruleManager;

    @Override
    public boolean canHandle(File artifact) {
        LOGGER.debug("WorkflowDeployer.canHandle(\"{}\")", artifact.getAbsolutePath());
        String fileEnding = FilenameUtils.getExtension(artifact.getName());

        boolean acceptedExtension = fileEnding.equals(RULE_ENDING) || fileEnding.equals(PROCESS_ENDING) || fileEnding
                .equals(FUNCTION_ENDING);
        if (artifact.isFile() && acceptedExtension) {
            LOGGER.info("found \"{}\" to deploy.", artifact);
            return true;
        }
        return false;
    }

    @Override
    public void install(File artifact) throws Exception {
        LOGGER.debug("WorkflowDeployer.install(\"{}\")", artifact.getAbsolutePath());
        RuleBaseElementId id = getIdforFile(artifact);
        String code = FileUtils.readFileToString(artifact);
        ruleManager.addOrUpdate(id, code);
        if (id.getType().equals(RuleBaseElementType.Process)) {
            cache.put(artifact.getName(), id);
        }
        LOGGER.info("Successfully installed workflow file \"{}\"", artifact.getName());
    }

    @Override
    public void update(File artifact) throws Exception {
        LOGGER.debug("WorkflowDeployer.update(\"{}\")", artifact.getAbsolutePath());
        RuleBaseElementId id = getIdforFile(artifact);
        String code = FileUtils.readFileToString(artifact);
        boolean changed = false;
        if (id.getType().equals(RuleBaseElementType.Process)) {
            RuleBaseElementId cachedId = cache.get(artifact.getName());
            if (!id.equals(cachedId)) {
                ruleManager.delete(cachedId);
                changed = true;
            }
        }
        ruleManager.addOrUpdate(id, code);
        if (changed) {
            cache.put(artifact.getName(), id);
        }
        LOGGER.info("Successfully updated workflow file \"{}\"", artifact.getName());
    }

    @Override
    public void uninstall(File artifact) throws Exception {
        LOGGER.debug("WorkflowDeployer.uninstall(\"{}\")", artifact.getAbsolutePath());
        RuleBaseElementId id = getIdforFile(artifact);

        if (id.getType().equals(RuleBaseElementType.Process)) {
            id = cache.remove(artifact.getName());
        }
        ruleManager.delete(id);
        LOGGER.info("Successfully deleted workflow file \"{}\"", artifact.getName());
    }

    private RuleBaseElementId getIdforFile(File artifact) throws Exception {
        RuleBaseElementType type = getTypeFromFile(artifact);
        String name = FilenameUtils.removeExtension(artifact.getName());
        RuleBaseElementId id = new RuleBaseElementId(type, name);
        if (artifact.exists() && type.equals(RuleBaseElementType.Process)) {
            id.setPackageName(readPackageNameFromProcessFile(artifact));
        }
        return id;
    }

    private RuleBaseElementType getTypeFromFile(File file) {
        String fileEnding = FilenameUtils.getExtension(file.getName());

        if (fileEnding.equals(FUNCTION_ENDING)) {
            return RuleBaseElementType.Function;
        }
        if (fileEnding.equals(RULE_ENDING)) {
            return RuleBaseElementType.Rule;
        }
        if (fileEnding.equals(PROCESS_ENDING)) {
            return RuleBaseElementType.Process;
        }

        throw new RuntimeException("rule type can not be resolved!");
    }

    private String readPackageNameFromProcessFile(File file) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        return doc.getDocumentElement().getAttribute(PACKAGE_ATTR);
    }

    public void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

}
