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

package org.openengsb.edb.core.test.unit.repository.jgit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.repository.Commit;
import org.openengsb.edb.core.repository.Repository;
import org.openengsb.edb.core.repository.RepositoryFactory;
import org.openengsb.edb.core.repository.Reset;
import org.openengsb.util.Prelude;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:edbBeans.xml" })
public class ResetTest {

    private Reset reset;
    private Repository repo;

    @Resource
    private RepositoryFactory gitRepositoryFactory;

    private static String repoPath;
    private static final String REPONAME = "reset-repo";

    private static final String MODE = "hard";
    private static final String AUTHOR = "edb";
    private static final String EMAIL = "edb@engsn.tuwien.ac.at";
    private static final String MSG = "test message";
    private static final String CONTENT_PATH = "some";
    private static final int DEPTH = 1;
    private static final UUID UUID_1 = new UUID(1, 1);
    private static final UUID UUID_2 = new UUID(2, 2);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // StringBuilder pathBuilder = new StringBuilder();
        // pathBuilder.append(System.getProperty("user.dir"));
        // pathBuilder.append(File.separator).append(REPONAME);
        // repoPath = pathBuilder.toString();
        ResetTest.repoPath = ResetTest.REPONAME;
    }

    @Before
    public void setUp() throws Exception {
        // actually create the repo
        this.repo = this.gitRepositoryFactory.loadRepository(ResetTest.REPONAME);

        Commit commit = this.repo.prepareCommit();
        GenericContent con1 = new GenericContent(ResetTest.repoPath, Prelude.dePathize("path"), Prelude
                .dePathize(ResetTest.CONTENT_PATH), ResetTest.UUID_1);
        con1.setProperty("key", "value");
        con1.store();
        GenericContent con2 = new GenericContent(ResetTest.repoPath, Prelude.dePathize("path"), Prelude
                .dePathize(ResetTest.CONTENT_PATH), ResetTest.UUID_2);
        con2.setProperty("key", "value1");
        con2.store();

        commit.add(new GenericContent[] { con1, });
        commit.setAuthor(ResetTest.AUTHOR, ResetTest.EMAIL);
        commit.setMessage(ResetTest.MSG);
        commit.commit();
        commit = this.repo.prepareCommit();
        commit.add(new GenericContent[] { con2, });
        commit.setAuthor(ResetTest.AUTHOR, ResetTest.EMAIL);
        commit.setMessage(ResetTest.MSG);
        commit.commit();

    }

    @After
    public void tearDown() {
        this.repo.removeRepository();
    }

    @Test
    public void testResetHardOneStepFileCreate() {

        String prefix = ResetTest.repoPath + File.separator + ResetTest.CONTENT_PATH + File.separator;

        assertTrue(new File(prefix + ResetTest.UUID_1.toString()).exists());
        assertTrue(new File(prefix + ResetTest.UUID_2.toString()).exists());

        this.reset = this.repo.prepareReset();
        this.reset.setDepth(ResetTest.DEPTH);
        this.reset.setMode(ResetTest.MODE);
        this.reset.reset();

        assertTrue(new File(prefix + ResetTest.UUID_1.toString()).exists());
        assertFalse(new File(prefix + ResetTest.UUID_2.toString()).exists());

        this.reset = this.repo.prepareReset();
        this.reset.setDepth(ResetTest.DEPTH);
        this.reset.setMode(ResetTest.MODE);
        this.reset.reset();

        assertFalse(new File(prefix + ResetTest.UUID_1.toString()).exists());
        assertFalse(new File(prefix + ResetTest.UUID_2.toString()).exists());
    }

    @Test
    public void testResetHardOneStepFileEdit() {

        String prefix = ResetTest.repoPath + File.separator + ResetTest.CONTENT_PATH + File.separator;

        Commit commit = this.repo.prepareCommit();
        GenericContent con1 = new GenericContent(ResetTest.repoPath, Prelude.dePathize("path"), Prelude
                .dePathize(ResetTest.CONTENT_PATH), ResetTest.UUID_1);
        con1.setProperty("key", "value");
        con1.setProperty("key_new", "value_new");
        con1.store();

        commit.add(new GenericContent[] { con1, });
        commit.setAuthor(ResetTest.AUTHOR, ResetTest.EMAIL);
        commit.setMessage(ResetTest.MSG + "_new");
        commit.commit();

        assertTrue(new File(prefix + ResetTest.UUID_1.toString()).exists());
        Properties read = new Properties();

        Reader reader;
        try {
            reader = new FileReader(new File(prefix + ResetTest.UUID_1.toString()));
            read.load(reader);
            reader.close();
        } catch (IOException e1) {
            fail("I/O error or file not found.");
        }
        assertEquals(read.getProperty("key_new"), "value_new");

        this.reset = this.repo.prepareReset();
        this.reset.setDepth(ResetTest.DEPTH);
        this.reset.setMode(ResetTest.MODE);
        this.reset.reset();

        assertTrue(new File(prefix + ResetTest.UUID_1.toString()).exists());
        read = new Properties();
        try {
            reader = new FileReader(new File(prefix + ResetTest.UUID_1.toString()));
            read.load(reader);
            reader.close();
        } catch (IOException e) {
            fail("I/O error or file not found.");
        }

        assertNull(read.getProperty("key_new"));

    }

    public void setGitRepositoryFactory(RepositoryFactory gitRepositoryFactory) {
        this.gitRepositoryFactory = gitRepositoryFactory;
    }

}
