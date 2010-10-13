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

package org.openengsb.svn.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.annotation.Resource;

import org.codehaus.plexus.util.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.svn.UpdateResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:../test-classes/test-bean.xml" })
public class SvnUT {
    @Resource
    private SvnConnectorExtension connector;

    private static String name2;
    private static String name1;

    @BeforeClass
    public static void startUp() {
        name2 = "" + System.currentTimeMillis();
        name1 = "0" + name2;
        System.out.println("xyz");
    }

    /**
     * Delete local working copy
     */
    @AfterClass
    public static void tearDown() throws IOException {
        FileUtils.deleteDirectory("data");
    }

    @Test
    public void testCheckoutRepository() {
        assertTrue(connector.checkout() > 0);
    }

    @Test
    public void testEmptyUpdate() {
        UpdateResult result = connector.update();

        assertTrue(result.getAddedBranches().size() == 0);
        assertTrue(result.getAddedTags().size() == 0);
        assertTrue(result.getCommitted().size() == 0);
        assertTrue(result.getDeletedBranches().size() == 0);
        assertTrue(result.getDeletedTags().size() == 0);
    }

    @Test
    public void testCreateDirs() {

        connector.createDir(name1, "test", true);
        connector.createDir(name1, "test", false);
        connector.createDir(name2, "test", true);
        connector.createDir(name2, "test", false);
        UpdateResult result = connector.update();

        assertTrue(result.getCommitted().size() == 0);
        assertTrue(result.getDeletedBranches().size() == 0);
        assertTrue(result.getDeletedTags().size() == 0);
        assertTrue(result.getAddedBranches().size() == 2);
        assertTrue(result.getAddedTags().size() == 2);

        assertTrue(result.getAddedBranches().get(0).equals(
                new File("./data/openengsb/test/branches/" + name1).getAbsolutePath()));
        assertTrue(result.getAddedTags().get(0).equals(
                new File("./data/openengsb/test/tags/" + name1).getAbsolutePath()));
        assertTrue(result.getAddedBranches().get(1).equals(
                new File("./data/openengsb/test/branches/" + name2).getAbsolutePath()));
        assertTrue(result.getAddedTags().get(1).equals(
                new File("./data/openengsb/test/tags/" + name2).getAbsolutePath()));
    }

    @Test
    public void testCommit() throws IOException {
        connector.createDir("temp", "test", true);
        connector.createDir("temp", "test", false);

        connector.update();

        copyTestFileTo("branches/temp");
        copyTestFileTo("tags/temp");
        copyTestFileTo("trunk");

        connector.add("branches/temp/text.txt");
        connector.add("tags/temp/text.txt");
        connector.add("trunk/text.txt");

        connector.commit("test", null);

        UpdateResult result = connector.update();
        assertTrue(result.getCommitted().size() == 3);
        assertTrue(result.getAddedBranches().size() == 0);
        assertTrue(result.getAddedTags().size() == 0);
        assertTrue(result.getDeletedBranches().size() == 0);
        assertTrue(result.getDeletedTags().size() == 0);

        assertTrue(result.getCommitted().get(0).equals(
                new File("./data/openengsb/test/branches/temp").getAbsolutePath()));
        assertTrue(result.getCommitted().get(1).equals(new File("./data/openengsb/test/trunk").getAbsolutePath()));
        assertTrue(result.getCommitted().get(2).equals(new File("./data/openengsb/test/tags/temp").getAbsolutePath()));

        connector.delete("branches/temp");
        connector.delete("tags/temp");
        connector.delete("trunk/text.txt");

        connector.commit("test", null);
    }

    private void copyTestFileTo(String target) throws IOException {
        File inputFile = new File("./target/test-classes/text.txt");
        File outputFile = new File("./data/openengsb/test/" + target + "/text.txt");

        FileReader in = new FileReader(inputFile);
        FileWriter out = new FileWriter(outputFile);
        int c;

        while ((c = in.read()) != -1)
            out.write(c);

        in.close();
        out.close();
    }

}
