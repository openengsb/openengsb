/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.edb.core.test.unit.lucene;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openengsb.edb.core.entities.GenericContent;


public abstract class ATestStub {

    /**
     * Build a number of GenericContent instances, use to dump some test-data
     * (not persisted on the file system).
     * 
     * @param number - amount of instances to create
     * @param fieldCount - number of properties per instance
     * @return List of all created GenericContent instances
     */
    public static List<GenericContent> buildGC(int number, int fieldCount, String path) {
        List<GenericContent> result = new ArrayList<GenericContent>();

        for (int i = 0; i < number; i++) {
            GenericContent e = new GenericContent(path, new String[] { "path1" }, new String[] { "", });
            for (int j = 0; j < fieldCount; j++) {
                e.setProperty(String.valueOf(j), UUID.randomUUID().toString());
            }
            e.setProperty("uuid", String.valueOf(i));
            result.add(e);
        }
        return result;
    }
}
