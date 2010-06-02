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
package org.openengsb.drools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;

public class DirectoryRuleSource implements RuleBaseSource {

    private String path;

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
        if (this.path == null) {
            throw new IllegalStateException("path must be set");
        }
        final PackageBuilder builder = new PackageBuilder();
        Collection<File> ruleFiles = findAllDrlFiles(new File(this.path));
        for (File f : ruleFiles) {
            try {
                builder.addPackageFromDrl(new FileReader(f));
            } catch (DroolsParserException e) {
                throw new RuleBaseException(e);
            } catch (FileNotFoundException e) {
                throw new RuleBaseException(e);
            } catch (IOException e) {
                throw new RuleBaseException(e);
            }
        }

        final RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage(builder.getPackage());

        return ruleBase;
    }

    private Collection<File> findAllDrlFiles(File dir) {
        if (!dir.isDirectory()) {
            return null;
        }
        List<File> result = new LinkedList<File>();
        for (File f : getDrlFiles(dir)) {
            result.add(f);
        }
        for (File f : getSubDirs(dir)) {
            result.addAll(findAllDrlFiles(f));
        }
        return result;
    }

    private File[] getSubDirs(File dir) {
        return dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
    }

    private File[] getDrlFiles(File dir) {
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".drl");
            }
        });
    }

}
