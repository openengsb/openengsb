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
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.lib.Commit;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.GitIndex;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.WorkDirCheckout;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.repository.RepositoryStateException;
import org.openengsb.edb.core.repository.Reset;

/**
 * Implementation of the {@link Reset} interface for a git-repository. Currently
 * only --hard and one step back are supported Copied from / based on
 * org.eclipse.egit.core.op.ResetOperation
 */
@SuppressWarnings("deprecation")
public class GitReset implements Reset {

    private Log log = LogFactory.getLog(getClass());

    private Repository repository;
    private GitIndex index;

    private Commit commit;
    private Tree newTree;

    private int depth = 1;
    private String mode = "hard";
    private final static String HEAD = Constants.HEAD;

    /**
     * Creates a new reset object. This requires the repository and the
     * repositoryPath at least. Furthermore since the "author" of a reset can
     * vary always the full identification is required. This is defined by the
     * {@link GitRepository} object and should not change for a repository.
     */
    public GitReset(Repository repository) {
        this.repository = repository;

        try {
            this.index = this.repository.getIndex();
        } catch (IOException e) {
            throw new RepositoryStateException("Cant retrieve index...", e);
        }
    }

    @Override
    public String reset() throws RepositoryStateException {
        try {
            if (this.depth < 1) {
                this.commit = this.repository.mapCommit(GitReset.HEAD);
                this.log.info("Reset depth lower then one, selecting 'HEAD' to reset to.");
            } else {
                this.commit = this.repository.mapCommit(GitReset.HEAD + "~" + this.depth);
                this.log.info("Selecting version " + this.depth + " steps behind 'HEAD'.");
            }

            RefUpdate ru = this.repository.updateRef(GitReset.HEAD);
            ru.setNewObjectId(this.commit.getCommitId());
            ru.setRefLogMessage("reset", false);
            if (ru.forceUpdate() == RefUpdate.Result.LOCK_FAILURE) {
                throw new RepositoryStateException("Can't update " + ru.getName());
            }
            this.log.debug("RefUpdate prepared.");

            this.newTree = this.commit.getTree();
            this.index = this.repository.getIndex();
            this.index.write();
            this.log.debug("Index updated");

            File parentFile = this.repository.getWorkDir();
            WorkDirCheckout workDirCheckout = new WorkDirCheckout(this.repository, parentFile, this.index, this.newTree);
            workDirCheckout.setFailOnConflict(false);
            workDirCheckout.checkout();
            this.index.write();
            this.log.debug("workdir resetted");

            try {
                writeReflogs();
            } catch (EDBException e) {
                throw new RepositoryStateException(e.getMessage());
            }

            this.log.info("Reset successfull.");

            return this.commit.getCommitId().name();
        } catch (IOException e) {
            throw new RepositoryStateException("reset failed: " + e.getMessage());
        }

    }

    public Reset setDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public Reset setMode(String mode) {
        this.mode = mode;
        return this;
    }

    private void writeReflog(String reflogRelPath) throws EDBException {
        try {
            final RefUpdate ru = repository.updateRef(Constants.HEAD);
            ru.setNewObjectId(commit.getCommitId());
            String name = this.commit.getCommitId().name();
            if (name.startsWith("refs/heads/")) //$NON-NLS-1$
                name = name.substring(11);
            if (name.startsWith("refs/remotes/")) //$NON-NLS-1$
                name = name.substring(13);
            String message = "reset --" + this.mode + " " + name;
            ru.setRefLogMessage(message, false);
            if (ru.forceUpdate() == RefUpdate.Result.LOCK_FAILURE) {
                throw new EDBException("Ref update failed because of locking failure.");
            }
        } catch (IOException e) {
            throw new EDBException(e.getMessage());
        }
    }

    private void writeReflogs() throws EDBException {
        try {
            writeReflog(Constants.HEAD);
            writeReflog(this.repository.getFullBranch());
        } catch (IOException e) {
            throw new EDBException("Writing reflogs", e);
        }
    }
}
