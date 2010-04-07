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

package org.openengsb.edb.core.repository.jgit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jgit.lib.Constants;
import org.openengsb.edb.core.repository.Commit;
import org.openengsb.edb.core.repository.Repository;
import org.openengsb.edb.core.repository.RepositoryManagementException;
import org.openengsb.edb.core.repository.RepositoryStateException;
import org.openengsb.edb.core.repository.Reset;
import org.openengsb.util.IO;

/**
 * Class to allow basic operations on given repositories: checkout, add
 * (remove), commit. Needs to be initialized with a repository location.
 */
public class GitRepository implements Repository {

    private static final String GIT_CONFIG = ".git";

    private static final String USER = "EDB";
    private static final String EMAIL = "edb@openengsb.org";

    private org.eclipse.jgit.lib.Repository repository;
    private File repositoryPath;

    @Override
    public GitRepository create(String repositoryId) throws RepositoryStateException, RepositoryManagementException {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(repositoryId).append(File.separator);
        this.repositoryPath = new File(pathBuilder.toString());

        try {
            this.repository = new org.eclipse.jgit.lib.Repository(new File(pathBuilder.append(GitRepository.GIT_CONFIG)
                    .toString()));
        } catch (IOException e) {
            throw new RepositoryManagementException("Cant create a new repository...", e);
        }

        if (this.repositoryPath.exists()) {
            return this;
        }

        try {
            this.repository.create();
            Commit commit = prepareCommit().setAuthor(GitRepository.USER, GitRepository.EMAIL).setMessage(
                    "Repository Setup");
            File f = new File(pathBuilder.delete(pathBuilder.length() - 4, pathBuilder.length())
                    .append("DO NOT DELETE").toString());
            f.createNewFile();
            commit.add(f);
            commit.commit();
            commit = prepareCommit().setAuthor(GitRepository.USER, GitRepository.EMAIL).setMessage("Repository Setup");
            f = new File(pathBuilder.toString());
            FileWriter writer = new FileWriter(f);
            writer.write("DO NOT DELETE");
            writer.close();
            commit.add(f);
            commit.commit();
        } catch (IllegalStateException e) {
            throw new RepositoryManagementException("Repository already exists and is in illegal state...", e);
        } catch (IOException e) {
            throw new RepositoryManagementException("Cant create a new repository...", e);
        }

        return this;
    }

    @Override
    public Commit prepareCommit() {
        return new GitCommit(this.repository, GitRepository.USER, GitRepository.EMAIL, this.repositoryPath).setAuthor(
                GitRepository.USER, GitRepository.EMAIL);
    }

    @Override
    public void removeRepository() {
        if (!IO.deleteStructure(this.repositoryPath)) {
            throw new RuntimeException("delete failed");
        }
    }

    @Override
    public File getRepositoryBase() {
        return this.repositoryPath;
    }

    @Override
    public Reset prepareReset() {
        return new GitReset(this.repository);
    }

    @Override
    public String getHeadRevision() throws RepositoryStateException {
        try {
            return this.repository.resolve(Constants.HEAD).name();
        } catch (IOException e) {
            throw new RepositoryManagementException("Repository is in illegal state ...", e);
        }
    }

}
