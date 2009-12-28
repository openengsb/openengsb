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

package org.openengsb.edb.core.entities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.util.IO;


public class GenericContentIOTest {

    private static final String TESTPATH = "easy2find";

    private static final int number = 1000;
    private static final int fields = 100;

    @Test
    public void testStoreASingleBlock() {
        Long before = System.nanoTime();
        List<GenericContent> contents = generateGC(GenericContentIOTest.number, GenericContentIOTest.fields);
        for (final GenericContent ct : contents) {
            ct.store();
        }
        Long after = System.nanoTime();
        System.out.println((after - before) / 1000000000 + " seconds for " + GenericContentIOTest.number
                + " entries with " + GenericContentIOTest.fields + " fields");
        IO.deleteStructure(new File(GenericContentIOTest.TESTPATH));
    }

    private static List<GenericContent> generateGC(int number, int fields) {
        List<GenericContent> result = new ArrayList<GenericContent>(number);

        GenericContent elem;
        for (int i = 0; i < number; i++) {
            elem = new GenericContent(GenericContentIOTest.TESTPATH, new String[] { "path", }, new String[] { "path"
                    + (i % 10), });
            for (int j = 0; j < fields; j++) {
                elem.setProperty(String.valueOf(j), UUID.randomUUID().toString());
            }
            result.add(elem);
        }

        return result;
    }

}
