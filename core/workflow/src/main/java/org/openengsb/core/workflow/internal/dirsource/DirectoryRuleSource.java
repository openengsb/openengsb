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

package org.openengsb.core.workflow.internal.dirsource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.openengsb.core.workflow.RuleBaseException;
import org.openengsb.core.workflow.internal.AbstractRuleManager;
import org.openengsb.core.workflow.internal.ResourceHandler;
import org.openengsb.core.workflow.model.RuleBaseElementId;
import org.openengsb.core.workflow.model.RuleBaseElementType;

public class DirectoryRuleSource extends AbstractRuleManager {

    private static Log log = LogFactory.getLog(DirectoryRuleSource.class);

    public static final String IMPORTS_FILENAME = "imports";

    public static final String GLOBALS_FILENAME = "globals";

    public static final String FUNC_EXTENSION = "func";

    public static final String RULE_EXTENSION = "rule";

    public static final String FLOW_EXTENSION = "rf";

    private String path;
    private File importsFile;

    private KnowledgeBase ruleBase;
    private boolean initialized = false;

    private String prelude;

    public DirectoryRuleSource() {
    }

    public DirectoryRuleSource(String path) {
        setPath(path);
    }

    public void init() throws RuleBaseException {
        if (this.ruleBase == null) {
            ruleBase = KnowledgeBaseFactory.newKnowledgeBase();
            readRuleBase();
            initReloadListener();
        }
        initialized = true;
    }

    private void initReloadListener() {
        Timer t = new Timer(true);
        t.schedule(new ReloadChecker(new File(path, "reload"), this), 0, 5000);
    }

    public final String getPath() {
        return this.path;
    }

    public final void setPath(String path) {
        this.path = path;
        importsFile = new File(path, IMPORTS_FILENAME);
    }

    @Override
    public KnowledgeBase getRulebase() {
        if (!initialized) {
            log.warn("rulebase not initialized. initializing now...");
            try {
                init();
            } catch (RuleBaseException e) {
                throw new IllegalStateException(e);
            }
        }
        return ruleBase;
    }

    @Override
    protected ResourceHandler<?> getRessourceHandler(RuleBaseElementType e) {
        switch (e) {
            case Rule:
                return new DirectoryRuleHandler(this);
            case Function:
                return new DirectoryFunctionHandler(this);
            case Global:
                return new DirectoryGlobalHandler(this);
            case Process:
                return new DirectoryProcessHandler(this);
            default:
                throw new UnsupportedOperationException("operation not implemented for type " + e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addImport(String className) throws RuleBaseException {
        List<String> lines;
        try {
            lines = FileUtils.readLines(importsFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (lines.contains(className)) {
            return;
        }
        lines.add(className);
        try {
            FileUtils.writeLines(importsFile, lines);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        readRuleBase();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> listImports() {
        try {
            return FileUtils.readLines(importsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeImport(String className) throws RuleBaseException {
        List<String> lines;
        try {
            lines = FileUtils.readLines(importsFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        lines.remove(className);
        try {
            FileUtils.writeLines(importsFile, lines);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        readRuleBase();
    }

    public void readRuleBase() throws RuleBaseException {
        if (this.path == null) {
            throw new IllegalStateException("path must be set");
        }
        File pathFile = new File(path);
        Collection<KnowledgePackage> packages;
        try {
            if (!pathFile.exists()) {
                initRuleBase();
            }
            String imports = readImportsFromRulebaseDirectory();
            String globals = readGlobalsFromRulebaseDirectory();
            prelude = imports + globals;

            Collection<File> dirs = listAllSubDirs(new File(this.path));

            packages = new LinkedList<KnowledgePackage>();
            for (File dir : dirs) {
                Collection<KnowledgePackage> p = doReadPackage(dir);
                packages.addAll(p);
            }
        } catch (IOException e) {
            throw new RuleBaseException(e);
        }

        for (KnowledgePackage ep : ruleBase.getKnowledgePackages()) {
            ruleBase.removeKnowledgePackage(ep.getName());
        }
        ruleBase.addKnowledgePackages(packages);
    }

    private void initRuleBase() throws IOException {
        File pathFile = new File(path);
        pathFile.mkdirs();
        URL defaultImports = this.getClass().getClassLoader().getResource("rulebase/imports");
        URL defaultglobals = this.getClass().getClassLoader().getResource("rulebase/globals");
        URL helloWorldRule = this.getClass().getClassLoader().getResource("rulebase/org/openengsb/hello1.rule");

        FileUtils.copyURLToFile(defaultImports, new File(path, IMPORTS_FILENAME));
        FileUtils.copyURLToFile(defaultglobals, new File(path, GLOBALS_FILENAME));
        File packagePath = new File(path, "org/openengsb/hello1.rule");
        packagePath.getParentFile().mkdirs();
        FileUtils.copyURLToFile(helloWorldRule, packagePath);
    }

    public void readPackage(String packageName) throws RuleBaseException {
        File dir = new File(this.path, packageName.replace(".", File.separator));
        Collection<KnowledgePackage> p;
        try {
            p = doReadPackage(dir);
        } catch (IOException e) {
            throw new RuleBaseException(e);
        }
        if (ruleBase.getKnowledgePackage(packageName) != null) {
            ruleBase.removeKnowledgePackage(packageName);
        }
        ruleBase.addKnowledgePackages(p);
    }

    private Collection<KnowledgePackage> doReadPackage(File path) throws IOException, RuleBaseException {
        StringBuffer content = new StringBuffer();
        content.append(String.format("package %s;\n", getPackageName(path)));
        content.append(prelude);
        Collection<File> functions = listFiles(path, FUNC_EXTENSION);
        for (File f : functions) {
            String func = FileUtils.readFileToString(f);
            content.append(func);
        }
        Collection<File> rules = listFiles(path, RULE_EXTENSION);
        for (File f : rules) {
            String ruleName = FilenameUtils.getBaseName(f.getName());
            content.append(String.format("rule \"%s\"\n", ruleName));
            content.append(FileUtils.readFileToString(f));
            content.append("end\n");
        }

        Properties properties = new Properties();
        properties.setProperty("drools.dialect.java.compiler", "JANINO");
        PackageBuilderConfiguration conf = new PackageBuilderConfiguration(properties);
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder(conf);

        builder.add(ResourceFactory.newReaderResource(new StringReader(content.toString())), ResourceType.DRL);
        Collection<File> flows = listFiles(path, FLOW_EXTENSION);
        for (File f : flows) {
            builder.add(ResourceFactory.newFileResource(f), ResourceType.DRF);
        }

        if (builder.hasErrors()) {
            System.out.println(content.toString());
            throw new RuleBaseException(builder.getErrors().toString());
        }

        return builder.getKnowledgePackages();

    }

    @SuppressWarnings("unchecked")
    private Collection<File> listFiles(File path, String extension) {
        Collection<File> functions = FileUtils.listFiles(path, new String[]{ extension }, false);
        return functions;
    }

    private String getPackageName(File file) {
        String filePath = file.getAbsolutePath();
        File path = new File(this.path);
        String name = filePath.substring(path.getAbsolutePath().length() + 1);
        return name.replace(File.separator, ".");
    }

    private static Collection<File> listAllSubDirs(File file) {
        Collection<File> result = new LinkedList<File>();
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                result.add(f);
                result.addAll(listAllSubDirs(f));
            }
        }
        return result;
    }

    private String readGlobalsFromRulebaseDirectory() throws IOException {
        File globalsFile = new File(this.path + File.separator + GLOBALS_FILENAME);
        StringBuffer result = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(globalsFile));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                result.append("global ");
                result.append(line);
                result.append('\n');
            }
        }
        reader.close();
        return result.toString();
    }

    private String readImportsFromRulebaseDirectory() throws IOException {
        File importsfile = new File(this.path + File.separator + IMPORTS_FILENAME);
        StringBuffer result = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(importsfile));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                result.append("import ");
                result.append(line);
                result.append('\n');
            }
        }
        reader.close();
        return result.toString();
    }

    public File getFilePath(RuleBaseElementId id) {
        switch (id.getType()) {
            case Global:
                return new File(this.path, GLOBALS_FILENAME);
            case Function:
                return new File(this.path, getPathName(id) + FUNC_EXTENSION);
            case Rule:
                return new File(this.path, getPathName(id) + RULE_EXTENSION);
            case Process:
                return new File(this.path, getPathName(id) + FLOW_EXTENSION);
            default:
                return null;
        }
    }

    private String getPathName(RuleBaseElementId id) {
        StringBuffer result = new StringBuffer();
        result.append(id.getPackageName().replace('.', File.separatorChar));
        result.append(File.separator);
        result.append(id.getName());
        result.append('.');
        return result.toString();
    }
}
