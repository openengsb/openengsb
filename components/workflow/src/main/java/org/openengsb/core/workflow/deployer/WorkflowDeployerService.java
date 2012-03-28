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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

/**
 * {@code ArtifactInstaller} that deploys workflow files
 */
public class WorkflowDeployerService extends AbstractOpenEngSBService implements ArtifactInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowDeployerService.class);
    private static final String RULE_ENDING = "rule";
    private static final String PROCESS_ENDING = "rf";
    private static final String FUNCTION_ENDING = "function";
    private static final String GLOBAL_ENDING = "global";
    private static final String IMPORT_ENDING = "import";
    private static final Set<String> SUPPORTED_ENDINGS = Sets.newHashSet(RULE_ENDING, PROCESS_ENDING, FUNCTION_ENDING,
        GLOBAL_ENDING, IMPORT_ENDING);
    private static final Map<String, RuleBaseElementType> ELEMENT_TYPES = ImmutableMap.of(
        RULE_ENDING, RuleBaseElementType.Rule,
        PROCESS_ENDING, RuleBaseElementType.Process,
        FUNCTION_ENDING, RuleBaseElementType.Function);
    private static final String PACKAGE_ATTR = "package-name";

    private static Map<String, RuleBaseElementId> cache = new HashMap<String, RuleBaseElementId>();

    private RuleManager ruleManager;

    @Override
    public boolean canHandle(File artifact) {
        LOGGER.debug("WorkflowDeployer.canHandle(\"{}\")", artifact.getAbsolutePath());
        String fileEnding = FilenameUtils.getExtension(artifact.getName());

        if (artifact.isFile() && SUPPORTED_ENDINGS.contains(fileEnding)) {
            LOGGER.info("found \"{}\" to deploy.", artifact);
            return true;
        }
        return false;
    }

    @Override
    public void install(File artifact) throws Exception {
        LOGGER.debug("WorkflowDeployer.install(\"{}\")", artifact.getAbsolutePath());
        try {
            doInstall(artifact);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        LOGGER.info("Successfully installed workflow file \"{}\"", artifact.getName());
    }

    private void doInstall(File artifact) throws Exception {
        String ending = FilenameUtils.getExtension(artifact.getName());
        RuleBaseElementType typeFromFile = getTypeFromFile(artifact);

        if (typeFromFile != null) {
            installRuleBaseElement(artifact);
        } else {
            if (IMPORT_ENDING.equals(ending)) {
                installImportFile(artifact);
            } else if (GLOBAL_ENDING.equals(ending)) {
                installGlobalFile(artifact);
            }
        }
    }

    private void installGlobalFile(File artifact) throws IOException {
        for (String importLine : FileUtils.readLines(artifact)) {
            String[] parts = importLine.split(" ");
            if (parts.length != 2) {
                continue;
            }
            ruleManager.addGlobal(parts[0], parts[1]);
        }
    }

    private void installImportFile(File artifact) throws IOException {
        for (String importLine : FileUtils.readLines(artifact)) {
            if (!importLine.isEmpty()) {
                ruleManager.addImport(importLine);
            }
        }
    }

    private void installRuleBaseElement(File artifact) throws Exception, IOException {
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
        try {
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
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        LOGGER.info("Successfully updated workflow file \"{}\"", artifact.getName());
    }

    @Override
    public void uninstall(File artifact) throws Exception {
        LOGGER.debug("WorkflowDeployer.uninstall(\"{}\")", artifact.getAbsolutePath());
        try {
            RuleBaseElementId id = getIdforFile(artifact);
            if(id == null){
                return;
            }
            if (id.getType().equals(RuleBaseElementType.Process)) {
                id = cache.remove(artifact.getName());
            }
            ruleManager.delete(id);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
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
        return ELEMENT_TYPES.get(fileEnding);
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
