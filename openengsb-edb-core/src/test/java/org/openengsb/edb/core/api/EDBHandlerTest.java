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

package org.openengsb.edb.core.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.util.IO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:edbBeans.xml" })
public class EDBHandlerTest {

    private static final String[] ABSTRACT_PATH = new String[] { "x", "y", "z" };
    private static final String[] ACTUAL_PATH_1 = new String[] { "a", "b", "c" };
    private static final String[] ACTUAL_PATH_2 = new String[] { "a", "b1", "c1" };

    @Resource
    private EDBHandlerFactory factory;

    private EDBHandler handler;

    private final UUID uuid = UUID.randomUUID();
    private GenericContent content;

    private String commitId;
    private String repoBase;

    @Before
    public void setUp() throws Exception {
        this.handler = this.factory.loadDefaultRepository();
        this.repoBase = this.handler.getRepositoryBase().getAbsolutePath();

        this.content = new GenericContent(this.repoBase, new String[] { "myKey" }, new String[] { "myValue" },
                this.uuid);

        this.handler.add(Arrays.asList(this.content));
        this.commitId = this.handler.commit("myUser", "myEmail");

    }

    @After
    public void tearDown() throws Exception {
        IO.deleteStructure(new File(this.repoBase).getParentFile());
    }

    @Test
    public void testQueryWithoutHead() throws Exception {
        List<GenericContent> list = this.handler.query("*", false);

        assertEquals(1, list.size());
        assertEquals("content", "myValue", list.get(0).getProperty("myKey"));
    }

    @Test
    public void testQueryWithHead() throws Exception {
        List<GenericContent> list = this.handler.query("*", true);

        assertEquals(2, list.size());
        assertEquals("head info", this.commitId, list.get(0).getProperty("HEAD"));
        assertEquals("content", "myValue", list.get(1).getProperty("myKey"));
    }

    @Test
    public void testResetInvalid() throws Exception {
        try {
            this.handler.reset("invalid", 0);
            fail("Reset performed despite invalid headId");
        } catch (EDBException e) {
            // expected
        }
    }

    @Test
    public void testResetDepth0() throws Exception {
        // add another revision
        GenericContent myContent = new GenericContent(this.repoBase, new String[] { "myKey" },
                new String[] { "yetAnotherValue" }, this.uuid);
        this.handler.add(Arrays.asList(myContent));
        String newCommitId = this.handler.commit("myUser", "myEmail");

        // reset by depth 0
        String retCommitId = this.handler.reset(newCommitId, 0);

        assertEquals("head info return value", newCommitId, retCommitId);

        // check content
        List<GenericContent> list = this.handler.query("*", true);

        assertEquals(2, list.size());
        assertEquals("query head info", newCommitId, list.get(0).getProperty("HEAD"));
        assertEquals("content", "yetAnotherValue", list.get(1).getProperty("myKey"));

    }

    @Test
    public void testResetDepth1() throws Exception {
        // add another revision
        GenericContent myContent = new GenericContent(this.repoBase, new String[] { "myKey" },
                new String[] { "yetAnotherValue" }, this.uuid);
        this.handler.add(Arrays.asList(myContent));
        String newCommitId = this.handler.commit("myUser", "myEmail");

        // reset by depth 1
        String retCommitId = this.handler.reset(newCommitId, 1);

        assertEquals("head info return value", this.commitId, retCommitId);

        // check content
        List<GenericContent> list = this.handler.query("*", true);

        assertEquals(2, list.size());
        assertEquals("query head info", this.commitId, list.get(0).getProperty("HEAD"));
        assertEquals("content", "myValue", list.get(1).getProperty("myKey"));

    }

    @Test
    public void testCommitEmpty() throws Exception {
        // no exception expected
        this.handler.commit("myUser", "myEmail");
    }

    @Test
    public void testCommit() throws Exception {
        // add another revision
        GenericContent myContent = new GenericContent(this.repoBase, new String[] { "myKey" },
                new String[] { "yetAnotherValue" }, this.uuid);
        this.handler.add(Arrays.asList(myContent));
        String newCommitId = this.handler.commit("myUser", "myEmail");

        // check content
        List<GenericContent> list = this.handler.query("*", true);

        assertEquals(2, list.size());
        assertEquals("head info", newCommitId, list.get(0).getProperty("HEAD"));
        assertEquals("content", "yetAnotherValue", list.get(1).getProperty("myKey"));
    }

    @Test
    public void testQueryNodesAtDepthZero() throws Exception {
        setupMoreCommits();
        List<GenericContent> result = this.handler.queryNodes(Arrays.asList(new String[] {}));
        assertEquals(2, result.size());
        List<String> results = new ArrayList<String>();
        // collect results
        results.add(result.get((0)).getProperty("name"));
        results.add(result.get((1)).getProperty("name"));

        assertTrue(results.contains("a"));
        assertTrue(results.contains("myValue"));
    }

    @Test
    public void testQueryNodesAtDepthOne() throws Exception {
        setupMoreCommits();
        List<GenericContent> result = this.handler.queryNodes(Arrays.asList(new String[] { "a", }));
        assertEquals(2, result.size());
        List<String> results = new ArrayList<String>();
        // collect results
        results.add(result.get((0)).getProperty("name"));
        results.add(result.get((1)).getProperty("name"));

        assertTrue(results.contains("b"));
        assertTrue(results.contains("b1"));

    }

    @Test
    public void testQueryNodesAtDepthTwo() throws Exception {
        setupMoreCommits();
        List<GenericContent> result = this.handler.queryNodes(Arrays.asList(new String[] { "a", "b" }));
        assertEquals(1, result.size());
        List<String> results = new ArrayList<String>();
        // collect results
        results.add(result.get((0)).getProperty("name"));

        assertTrue(results.contains("c"));

    }

    /**
     * @param factory the factory to set
     */
    @Autowired
    public void setFactory(EDBHandlerFactory factory) {
        this.factory = factory;
    }

    /**
     * Proper tests for query nodes requires some more nodes to exist
     */
    private void setupMoreCommits() throws Exception {

        List<GenericContent> list = new ArrayList<GenericContent>();
        list.add(new GenericContent(this.repoBase, ABSTRACT_PATH, ACTUAL_PATH_1));
        list.add(new GenericContent(this.repoBase, ABSTRACT_PATH, ACTUAL_PATH_2));

        this.handler.add(list);
        this.commitId = this.handler.commit("myUser", "myEmail");
    }
}
