/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.drools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.drools.CheckedDroolsException;
import org.drools.RuleBase;
import org.drools.compiler.RuleBaseLoader;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;

public class FileRuleSource implements RuleBaseSource {

    private String path;

    public final String getPath() {
        return this.path;
    }

    public final void setPath(String path) {
        this.path = path;
    }

    @Override
    public RuleBase getRulebase() {
        if (this.path == null) {
            throw new IllegalStateException("path must be set");
        }
        Resource rbaseResource = ResourceFactory.newClassPathResource(this.path);
        RuleBaseLoader loader = RuleBaseLoader.getInstance();
        InputStream is;

        try {
            is = rbaseResource.getInputStream();
            return loader.loadFromReader(new InputStreamReader(is));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize rulebase from path " + this.path, e);
        } catch (CheckedDroolsException e) {
            throw new IllegalStateException("Cannot initialize rulebase from path" + this.path, e);
        }
    }
}
