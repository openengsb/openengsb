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

package org.openengsb.edb.core.test.unit.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.edb.core.api.EDBHandlerFactory;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.repository.Repository;
import org.openengsb.edb.core.repository.RepositoryFactory;
import org.openengsb.edb.core.repository.jgit.GitRepositoryFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests to check if the repository wrapper works correctly.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:testBeans.xml" })
public class GitRepositoryTest {

    @Resource
    private EDBHandlerFactory factory;

    @Test
    public void testGitCommit() throws Exception {
        RepositoryFactory factory = new GitRepositoryFactory();
        Repository repo = factory.loadRepository("target/testRepo");

        GenericContent content = new GenericContent("target/testRepo", new String[] { "path1", "path2", "path3", },
                new String[] { "a", "b", "c", });
        content.store();

        repo.prepareCommit().add(content).setAuthor("Andreas Pieber", "anpi@gmx.at").setMessage("CommitA").commit();
        content.setProperty("first", "first");
        content.store();

        repo.prepareCommit().add(content).setAuthor("Andreas Pieber", "anpi@gmx.at").setMessage("CommitB").commit();
        content.setProperty("second", "second");
        content.store();

        repo.prepareCommit().add(content).setAuthor("Andreas Pieber", "anpi@gmx.at").setMessage("CommitC").commit();
        content.setProperty("third", "third");
        content.store();

        repo.prepareCommit().add(content).setAuthor("Andreas Pieber", "anpi@gmx.at").setMessage("CommitD").commit();

        repo.removeRepository();
    }

    @Test
    public void testRemoveRepository() throws Exception {
        RepositoryFactory factory = new GitRepositoryFactory();
        Repository repo = factory.loadRepository("target/testDeleteRepo");
        assertEquals(true, new File("target/testDeleteRepo").exists());
        repo.removeRepository();
        assertFalse(new File("target/testDeleteRepo").exists());
    }

}
