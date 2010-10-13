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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.UnmergedPathException;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public final class RepositoryFixture {

    private RepositoryFixture() {
    }

    public static FileRepository createRepository(File directory) throws Exception {
        FileRepository repository = create(directory);
        Git git = new Git(repository);
        addFile(git, "testfile");
        commit(git, "initial commit");
        return repository;
    }

    public static void addFile(Git git, String filename) throws IOException, NoFilepatternException {
        FileWriter writer = new FileWriter(new File(git.getRepository().getWorkTree(), filename));
        writer.write(filename + "\n");
        writer.close();
        AddCommand add = git.add();
        add.addFilepattern(filename).call();
    }

    public static void commit(Git git, String message) throws NoHeadException, NoMessageException,
        UnmergedPathException,
        ConcurrentRefUpdateException, WrongRepositoryStateException {
        CommitCommand commit = git.commit();
        commit.setMessage(message).call();
    }

    private static FileRepository create(File directory) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        FileRepository repository = builder.setWorkTree(directory).build();
        repository.create();
        return repository;
    }
}
