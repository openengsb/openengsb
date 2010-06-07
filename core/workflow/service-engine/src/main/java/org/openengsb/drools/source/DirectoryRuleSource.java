/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.drools.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;
import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.message.RuleBaseElement;
import org.openengsb.drools.source.dir.DirectoryFunctionHandler;
import org.openengsb.drools.source.dir.DirectoryGlobalHandler;
import org.openengsb.drools.source.dir.DirectoryImportHandler;
import org.openengsb.drools.source.dir.DirectoryRuleHandler;

public class DirectoryRuleSource extends RuleBaseSource {

    public static final String IMPORTS_FILENAME = "imports";

    public static final String GLOBALS_FILENAME = "globals";

    public static final String FUNC_EXTENSION = ".func";

    public static final String RULE_EXTENSION = ".rule";

    private String path;
    private RuleBase ruleBase;

    public DirectoryRuleSource() {
    }

    public DirectoryRuleSource(String path) {
        this.path = path;
    }

    public final String getPath() {
        return this.path;
    }

    public final void setPath(String path) {
        this.path = path;
    }

    @Override
    public RuleBase getRulebase() throws RuleBaseException {
        if (this.ruleBase == null) {
            ruleBase = RuleBaseFactory.newRuleBase();
            readRuleBase();
        }
        return this.ruleBase;
    }

    @Override
    protected ResourceHandler<?> getRessourceHandler(RuleBaseElement e) {
        switch (e) {
        case Rule:
            return new DirectoryRuleHandler(this);
        case Import:
            return new DirectoryImportHandler(this);
        case Function:
            return new DirectoryFunctionHandler(this);
        case Global:
            return new DirectoryGlobalHandler(this);
        default:
            throw new UnsupportedOperationException("operation not implemented for type " + e);
        }
    }

    public void readRuleBase() throws RuleBaseException {
        if (this.path == null) {
            throw new IllegalStateException("path must be set");
        }
        StringBuffer drl = new StringBuffer();
        drl.append(String.format("package %s \n", DEFAULT_RULE_PACKAGE));
        try {
            drl.append(readImportsFromRulebaseDirectory());
            drl.append(readGlobalsFromRulebaseDirectory());
            drl.append(readFunctionsFromRulebaseDirectory());
            drl.append(readRulesFromRulebaseDirectory());
        } catch (IOException e) {
            throw new RuleBaseException(e);
        }

        final PackageBuilder builder = new PackageBuilder();
        try {
            builder.addPackageFromDrl(new StringReader(drl.toString()));
        } catch (DroolsParserException e) {
            throw new RuleBaseException(e);
        } catch (IOException e) {
            throw new RuleBaseException(e);
        }

        if (builder.hasErrors()) {
            System.err.println(drl.toString());
            throw new RuleBaseException(builder.getErrors().toString());
        }

        Package p = builder.getPackage();
        ruleBase.lock();
        if (ruleBase.getPackages().length > 0) {
            ruleBase.removePackage(DEFAULT_RULE_PACKAGE);
        }
        ruleBase.addPackage(p);
        ruleBase.unlock();
    }

    private String readRulesFromRulebaseDirectory() throws IOException {
        StringBuffer result = new StringBuffer();
        for (File fuleFile : findAll(RULE_EXTENSION)) {
            result.append("rule \"");
            result.append(getElementName(fuleFile.getName()));
            result.append("\"\n");
            result.append(readFileContent(fuleFile));
            result.append("\nend\n");
        }
        return result.toString();
    }

    private String readFunctionsFromRulebaseDirectory() throws IOException {
        StringBuffer result = new StringBuffer();
        for (File functionFile : findAll(FUNC_EXTENSION)) {
            result.append(readFileContent(functionFile));
        }
        return result.toString();
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
                result.append("\n");
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
                result.append("\n");
            }
        }
        reader.close();
        return result.toString();
    }

    private File[] findAll(final String extension) {
        return new File(path).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(extension);
            }
        });
    }

    private String readFileContent(File file) throws IOException {
        StringBuffer result = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
            result.append("\n");
        }
        reader.close();
        return result.toString();
    }

    private String getElementName(String filename) {
        int lastindex = filename.lastIndexOf(".");
        return filename.substring(0, lastindex);
    }

}
