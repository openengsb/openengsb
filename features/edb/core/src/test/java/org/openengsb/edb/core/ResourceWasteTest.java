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

package org.openengsb.edb.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.edb.core.entities.GenericContent;

public class ResourceWasteTest {

    private static Log log = LogFactory.getLog(ResourceWasteTest.class);

    @Test
    @Ignore
    public void testGenerator() {
        long time = System.currentTimeMillis();
        List<GenericContent> bar = buildCE(5001, 50);

        time = System.currentTimeMillis() - time;
        ResourceWasteTest.log.info("\ncreate " + time);
        time = System.currentTimeMillis();
        for (GenericContent c : bar) {
            c.getEntireContent();
        }
        time = System.currentTimeMillis() - time;
        ResourceWasteTest.log.info("\niterate " + time);
    }

    @Test
    @Ignore
    public void testGeneratorTwo() {
        System.out.println("Map");
        long time = System.currentTimeMillis();
        List<Map<Integer, Integer>> bar = buildSomeThingElse(20001, 50);
        time = System.currentTimeMillis() - time;
        ResourceWasteTest.log.info("\ncreate " + time);
        time = System.currentTimeMillis();
        for (Map<Integer, Integer> c : bar) {
            c.get(2);
        }
        time = System.currentTimeMillis() - time;
        ResourceWasteTest.log.info("\niterate " + time);
    }

    @Test
    @Ignore
    public void testGeneratorThree() {
        ResourceWasteTest.log.info("List");
        long time = System.currentTimeMillis();
        List<List<Integer>> bar = buildAnotherSomeThingElse(110001, 100);
        time = System.currentTimeMillis() - time;
        System.out.println("\ncreate " + time);
        time = System.currentTimeMillis();
        for (List<Integer> c : bar) {
            c.get(2);
        }
        time = System.currentTimeMillis() - time;
        ResourceWasteTest.log.info("\niterate " + time);
    }

    private static List<GenericContent> buildCE(int number, int fieldCount) {
        List<GenericContent> result = new ArrayList<GenericContent>();

        for (int i = 0; i < number; i++) {
            GenericContent e = new GenericContent();

            for (int j = 0; j < fieldCount; j++) {
                e.setProperty(String.valueOf(j), UUID.randomUUID().toString());
            }

            result.add(e);

            if (i % 1000 == 0) {
                System.out.print(i + ",");
            }
            if (i % 10000 == 0) {
                ResourceWasteTest.log.info("");
            }
        }

        return result;
    }

    private static List<Map<Integer, Integer>> buildSomeThingElse(int number, int fieldCount) {
        List<Map<Integer, Integer>> result = new ArrayList<Map<Integer, Integer>>();

        for (int i = 0; i < number; i++) {

            Map<Integer, Integer> map = new HashMap<Integer, Integer>();

            for (int j = 0; j < fieldCount; j++) {
                map.put(j, i + j);
            }
            result.add(map);
            if (i % 10000 == 0) {
                System.out.print(i + ",");
            }
            if (i % 100000 == 0) {
                ResourceWasteTest.log.info("");
            }
        }
        return result;
    }

    private static List<List<Integer>> buildAnotherSomeThingElse(int number, int fieldCount) {
        List<List<Integer>> result = new ArrayList<List<Integer>>();

        for (int i = 0; i < number; i++) {

            List<Integer> list = new ArrayList<Integer>();

            for (int j = 0; j < fieldCount; j++) {
                list.add(j);
            }
            result.add(list);
            if (i % 10000 == 0) {
                System.out.print(i + ",");
            }
            if (i % 100000 == 0) {
                ResourceWasteTest.log.info("");
            }

        }
        return result;
    }

}
