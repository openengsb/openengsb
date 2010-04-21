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

package org.openengsb.edb.core.ut.api;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.api.EDBHandlerFactory;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.util.IO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:testBeans.xml" })
public class EDBHandlerUT {

    @Resource
    private EDBHandlerFactory factory;

    private EDBHandler handler;

    private final UUID uuid = UUID.randomUUID();
    private GenericContent content;

    private String repoBase;

    private static Log log = LogFactory.getLog(EDBHandlerUT.class);

    @Before
    public void setUp() throws Exception {
        handler = factory.loadDefaultRepository();
        repoBase = handler.getRepositoryBase().getAbsolutePath();

        content = new GenericContent(repoBase, new String[] { "myKey" }, new String[] { "myValue" }, uuid);

        handler.add(Arrays.asList(content));
    }

    @After
    public void tearDown() throws Exception {
        if (!IO.deleteStructure(new File(repoBase).getParentFile())) {
            throw new Exception("cleanup failed");
        }
    }

    @Test
    public void testMiniCommit() throws Exception {
        List<GenericContent> list = buildMassGC(10, 20, repoBase);
        long a = System.currentTimeMillis();
        long time = System.currentTimeMillis();
        handler.add(list);

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\nadd(ms) " + time);
        time = System.currentTimeMillis();

        // commit with 1000 elements with 20 properties each
        String newCommitId = handler.commit("myUser", "myEmail");

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\ncommit(ms) " + time);
        time = System.currentTimeMillis();

        // check content
        list = handler.query("*", true);

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\nsearch(ms) " + time);
        EDBHandlerUT.log.info("\n----test---- (ms) " + (System.currentTimeMillis() - a));
        assertEquals(12, list.size());
        assertEquals("head info", newCommitId, list.get(0).getProperty("HEAD"));
    }

    @Test
    public void testDualCommit() throws Exception {
        List<GenericContent> list = buildMassGC(3500, 20, repoBase);
        long a = System.currentTimeMillis();
        long time = System.currentTimeMillis();
        handler.add(list);

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\nadd(ms) " + time);
        time = System.currentTimeMillis();

        // commit with 1000 elements with 20 properties each
        String newCommitId = handler.commit("myUser", "myEmail");

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\ncommit(ms) " + time);
        time = System.currentTimeMillis();

        // check content
        list = handler.query("*", true);

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\nsearch(ms) " + time);
        EDBHandlerUT.log.info("\n----test---- (ms) " + (System.currentTimeMillis() - a));
        assertEquals(3502, list.size());
        assertEquals("head info", newCommitId, list.get(0).getProperty("HEAD"));

        list = buildMassGC(3500, 20, repoBase);
        a = System.currentTimeMillis();
        time = System.currentTimeMillis();
        handler.add(list);

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\nadd(ms) " + time);
        time = System.currentTimeMillis();

        // commit with 1000 elements with 20 properties each
        newCommitId = handler.commit("myUser", "myEmail");

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\ncommit(ms) " + time);
        time = System.currentTimeMillis();

        // check content
        list = handler.query("*", true);

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\nsearch(ms) " + time);
        EDBHandlerUT.log.info("\n----test---- (ms) " + (System.currentTimeMillis() - a));
        assertEquals(7002, list.size());
        assertEquals("head info", newCommitId, list.get(0).getProperty("HEAD"));
    }

    @Test
    public void testMassCommit() throws Exception {
        List<GenericContent> list = buildMassGC(7000, 20, repoBase);
        long a = System.currentTimeMillis();
        long time = System.currentTimeMillis();
        handler.add(list);

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\nadd(ms) " + time);
        time = System.currentTimeMillis();

        // commit with 1000 elements with 20 properties each
        String newCommitId = handler.commit("myUser", "myEmail");

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\ncommit(ms) " + time);
        time = System.currentTimeMillis();

        // check content
        list = handler.query("*", true);

        time = System.currentTimeMillis() - time;
        EDBHandlerUT.log.info("\nsearch(ms) " + time);
        EDBHandlerUT.log.info("\n----test---- (ms) " + (System.currentTimeMillis() - a));
        assertEquals(7002, list.size());
        assertEquals("head info", newCommitId, list.get(0).getProperty("HEAD"));
    }

    /**
     * @param factory the factory to set
     */
    @Autowired
    public void setFactory(EDBHandlerFactory factory) {
        this.factory = factory;
    }

    private static List<GenericContent> buildMassGC(int count, int fieldNumber, String basePath) {
        List<GenericContent> result = new ArrayList<GenericContent>();
        GenericContent gc;
        for (int i = 0; i < count; i++) {
            gc = new GenericContent(basePath, new String[] { "absPath" }, new String[] { "path" });
            for (int j = 0; j < fieldNumber; j++) {
                gc.setProperty(String.valueOf(j), UUID.randomUUID().toString());
            }
            result.add(gc);
        }
        return result;
    }

}
