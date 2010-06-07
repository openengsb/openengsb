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
package org.openengsb.drools.source.dir;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.source.DirectoryRuleSource;
import org.openengsb.drools.source.ResourceHandler;

public abstract class SingleFileResourceHandler extends ResourceHandler<DirectoryRuleSource> {

    protected File file;

    public SingleFileResourceHandler(DirectoryRuleSource source) {
        super(source);
        file = new File(source.getPath() + File.separator + getFileName());
    }

    public abstract String getFileName();

    protected Set<String> readFile() throws RuleBaseException {
        try {
            Reader reader = new FileReader(this.file);
            List<String> result = readLines(reader);
            reader.close();
            return new TreeSet<String>(result);
        } catch (IOException e) {
            throw new RuleBaseException("cannot read imports", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> readLines(Reader reader) throws IOException {
        List<String> result = IOUtils.readLines(reader);
        return result;
    }

    protected void writeFile(Collection<String> list) throws RuleBaseException {
        try {
            FileWriter writer = new FileWriter(file);
            IOUtils.writeLines(list, "\n", writer);
            writer.close();
        } catch (IOException e) {
            throw new RuleBaseException("cannot write imports", e);
        }
    }
}
