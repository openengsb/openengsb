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

package org.openengsb.edb.core.test.unit.repository;

import org.junit.Test;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.repository.Repository;
import org.openengsb.edb.core.repository.RepositoryFactory;
import org.openengsb.edb.core.repository.jgit.GitRepositoryFactory;

/**
 * Tests to check if the repository wrapper works correctly.
 */
public class GitRepositoryTest {

    @Test
    public void testGitCommit() throws Exception {
        RepositoryFactory fact = new GitRepositoryFactory();
        Repository repo = fact.loadRepository("target/testRepo");

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

}
