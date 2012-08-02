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
package org.openengsb.core.workflow.drools.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.ReferenceCounter;
import org.openengsb.core.workflow.api.RuleBaseException;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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

    private ReferenceCounter<String> importReferences = new ReferenceCounter<String>();
    private ReferenceCounter<String> globalReferences = new ReferenceCounter<String>();

    private RuleManager ruleManager;
    private BundleContext bundleContext;

    private Collection<File> failedArtifacts = Lists.newLinkedList();

    public void init() {
        bundleContext.addBundleListener(new BundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                if (event.getType() == BundleEvent.STARTED) {
                    try {
                        tryInstallingFailedArtifacts();
                    } catch (Exception e) {
                        LOGGER.error("error when trying to instal artifacts", e);
                    }
                }
            }
        });
    }

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
            LOGGER.info("Successfully installed workflow file \"{}\"", artifact.getName());
        } catch (RuleBaseException e) {
            LOGGER.warn("Could not deploy workflow-element {} because of unsatisfied dependencies", artifact.getName());
            LOGGER.debug(e.getMessage());
            LOGGER.debug("Details: ", e);
            failedArtifacts.add(artifact);
            return;
        } catch (Exception e) {
            LOGGER.error("Error when deploying workflow-element", e);
            throw e;
        }
        tryInstallingFailedArtifacts();
    }

    private void tryInstallingFailedArtifacts() throws Exception {
        Exception occured = null;
        synchronized (failedArtifacts) {
            Iterator<File> iterator = failedArtifacts.iterator();
            while (iterator.hasNext()) {
                File failed = iterator.next();
                try {
                    doInstall(failed);
                    iterator.remove();
                    iterator = failedArtifacts.iterator();
                } catch (RuleBaseException e) {
                    LOGGER.warn("Could not deploy workflow-element {} because of unsatisfied dependencies",
                        failed.getName());
                    LOGGER.info(e.getMessage());
                    LOGGER.debug("Details: ", e);
                } catch (Exception e) {
                    LOGGER.error("unexpected exception when trying to install " + failed.getName() + " delayed", e);
                    /*
                     * we still want to attempt installing the other artifacts. So we just record the Exception and
                     * throw it later.
                     */
                    occured = e;
                }
            }
        }
        if (occured != null) {
            throw occured;
        }
    }

    private void doInstall(File artifact) throws RuleBaseException, IOException, SAXException,
        ParserConfigurationException {
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
        try {
            for (String importLine : FileUtils.readLines(artifact)) {
                if (importLine.isEmpty() || importLine.startsWith("#")) {
                    continue;
                }
                String[] parts = importLine.split(" ");
                if (parts.length != 2) {
                    continue;
                }
                ruleManager.addGlobal(parts[0], parts[1]);
                globalReferences.addReference(artifact, parts[1]);
            }
        } catch (RuleBaseException e) {
            Set<String> garbage = globalReferences.removeFile(artifact);
            for (String globalName : garbage) {
                ruleManager.removeGlobal(globalName);
            }
            throw e;
        }
    }

    private void installImportFile(File artifact) throws IOException {
        for (String importLine : FileUtils.readLines(artifact)) {
            if (!importLine.isEmpty() && !importLine.startsWith("#")) {
                ruleManager.addImport(importLine);
                importReferences.addReference(artifact, importLine);
            }
        }
    }

    private void installRuleBaseElement(File artifact) throws RuleBaseException, IOException, SAXException,
        ParserConfigurationException {
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
            RuleBaseElementType typeFromFile = getTypeFromFile(artifact);
            String ending = FilenameUtils.getExtension(artifact.getName());
            if (typeFromFile != null) {
                doUpdateArtifact(artifact);
            } else if (IMPORT_ENDING.equals(ending)) {
                installImportFile(artifact);
            } else if (GLOBAL_ENDING.equals(ending)) {
                installGlobalFile(artifact);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        LOGGER.info("Successfully updated workflow file \"{}\"", artifact.getName());
    }

    private void doUpdateArtifact(File artifact) throws SAXException, IOException, ParserConfigurationException {
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
    }

    @Override
    public void uninstall(File artifact) throws Exception {
        LOGGER.debug("WorkflowDeployer.uninstall(\"{}\")", artifact.getAbsolutePath());
        try {
            doUninstall(artifact);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw e;
        }
        LOGGER.info("Successfully deleted workflow file \"{}\"", artifact.getName());
    }

    private void doUninstall(File artifact) throws Exception {
        RuleBaseElementType type = getTypeFromFile(artifact);
        if (type != null) {
            RuleBaseElementId id = getIdforFile(artifact);
            if (id.getType().equals(RuleBaseElementType.Process)) {
                id = cache.remove(artifact.getName());
            }
            ruleManager.delete(id);
            return;
        }
        String extension = FilenameUtils.getExtension(artifact.getName());
        if (IMPORT_ENDING.equals(extension)) {
            unInstallImportFile(artifact);
        } else if (GLOBAL_ENDING.equals(extension)) {
            unInstallGlobalFile(artifact);
        }
    }

    private void unInstallGlobalFile(File artifact) {
        Set<String> globalsGarbage = globalReferences.removeFile(artifact);
        for (String i : globalsGarbage) {
            ruleManager.removeGlobal(i);
        }
    }

    private void unInstallImportFile(File artifact) {
        Set<String> garbageImports = importReferences.removeFile(artifact);
        for (String i : garbageImports) {
            ruleManager.removeImport(i);
        }
    }

    private RuleBaseElementId getIdforFile(File artifact) throws RuleBaseException, SAXException, IOException,
        ParserConfigurationException {
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

    private String readPackageNameFromProcessFile(File file) throws SAXException, IOException,
        ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        return doc.getDocumentElement().getAttribute(PACKAGE_ATTR);
    }

    public void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
