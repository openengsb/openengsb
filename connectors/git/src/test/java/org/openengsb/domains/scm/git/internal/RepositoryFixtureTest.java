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

package org.openengsb.domains.scm.git.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RepositoryFixtureTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void createSampleDirectory_shoulHaveHiddenDirectoryDotGit() throws Exception {
        File folder = temporaryFolder.newFolder("sample");
        assertThat(folder.isDirectory(), is(true));
        FileRepository repository = RepositoryFixture.createRepository(folder);
        assertThat(new File(folder, ".git").isDirectory(), is(true));
        repository.close();
    }

    @Test
    public void createSampleDirectory_shouldHaveOneCommit() throws Exception {
        File folder = temporaryFolder.newFolder("sample");
        FileRepository repository = RepositoryFixture.createRepository(folder);
        assertThat(repository.getBranch(), is("master"));
        RevWalk walk = new RevWalk(repository);
        walk.markStart(walk.parseCommit(repository.resolve("refs/heads/master")));
        assertThat(walk.next(), notNullValue());
        assertThat(walk.next(), nullValue());
        repository.close();
    }
}
