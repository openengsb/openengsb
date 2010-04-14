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
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.GitIndex;
import org.eclipse.jgit.lib.IndexDiff;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectWriter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.TreeEntry;
import org.eclipse.jgit.lib.GitIndex.Entry;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.repository.Commit;
import org.openengsb.edb.core.repository.RepositoryStateException;

/**
 * Implementation of the {@link Commit} interface for a git-repository.
 */
@SuppressWarnings("deprecation")
public class GitCommit implements Commit {

    private Log log = LogFactory.getLog(getClass());

    private Repository repository;
    private GitIndex index;
    private File repositoryPath;

    private String commiterName;
    private String commiterEmail;
    private String commitMessage;
    private String authorName;
    private String authorEmail;

    /**
     * Creates a new commit object. This requires the repository and the
     * repositoryPath at least. Furthermore since the author of a commit can
     * vary always the full identification of an commiter is required. This is
     * defined by the {@link GitRepository} object and should not change for a
     * repository.
     */
    public GitCommit(Repository repository, String commiterName, String commiterEmail, File repositoryPath) {
        this.repository = repository;
        this.commiterName = commiterName;
        this.commiterEmail = commiterEmail;
        this.repositoryPath = repositoryPath;

        try {
            this.index = this.repository.getIndex();
        } catch (IOException e) {
            throw new RepositoryStateException("Cant retrieve index...", e);
        }
    }

    public Commit add(GenericContent... contents) {
        for (GenericContent content : contents) {
            try {
                this.index.add(this.repositoryPath, content.getFileLocation());
            } catch (IOException e) {
                throw new RepositoryStateException("Cant add generic content to index...", e);
            }
        }
        return this;
    }

    public Commit add(File... files) {
        for (File file : files) {
            try {
                this.index.add(this.repositoryPath, file);
            } catch (IOException e) {
                throw new RepositoryStateException("Cant add file to index...", e);
            }
        }
        return this;
    }

    public Commit delete(File... files) {
        for (File file : files) {
            try {
                this.index.remove(this.repositoryPath, file);
            } catch (IOException e) {
                throw new RepositoryStateException("Cant remove file from index...", e);
            }
        }
        return this;
    }

    public Commit delete(GenericContent... contents) {
        for (GenericContent content : contents) {
            try {
                this.index.remove(this.repositoryPath, content.getFileLocation());
            } catch (IOException e) {
                throw new RepositoryStateException("Cant remove generic content from index...", e);
            }
        }
        return this;
    }

    public String commit() throws RepositoryStateException {
        if (this.commiterEmail == null || this.commiterName == null || this.commitMessage == null
                || this.authorEmail == null || this.authorName == null) {
            throw new RepositoryStateException("Requiring all fields to be filled, before executing a commit...");
        }

        this.log.debug("Doing commit for author [" + this.authorName + "; " + this.authorEmail + "] with message ["
                + this.commitMessage + "]...");

        try {
            this.log.trace("Writing index...");
            this.index.write();
        } catch (IOException e) {
            throw new RepositoryStateException("Cant write index...", e);
        }

        IndexDiff diff;
        try {
            this.log.trace("Creating diff from index...");
            diff = new IndexDiff(this.repository.mapTree(Constants.HEAD), this.repository.getIndex());
            diff.diff();
        } catch (IOException e) {
            throw new RepositoryStateException("Cant write diff tree...", e);
        }

        HashSet<String> allFiles = new HashSet<String>();

        allFiles.addAll(diff.getAdded());
        allFiles.addAll(diff.getChanged());
        allFiles.addAll(diff.getModified());
        allFiles.addAll(diff.getRemoved());

        this.log.debug("[" + allFiles.size() + "] files changed and to commit...");

        try {

            this.log.trace("creating repository tree...");
            Tree projectTree = this.repository.mapTree(Constants.HEAD);
            if (projectTree == null) {
                projectTree = new Tree(this.repository);
            }

            this.log.trace("Iterating each files and write the trees for each of them...");
            for (String string : allFiles) {
                File actualFile = new File(this.repositoryPath.getAbsolutePath() + File.separator + string);

                this.log.debug("Writing file [" + actualFile.getAbsolutePath() + "] to index...");

                TreeEntry treeMember = projectTree.findBlobMember(string);
                if (treeMember != null) {
                    treeMember.delete();
                }

                this.log.trace("Getting entry index...");
                Entry idxEntry = this.index.getEntry(string);
                if (diff.getMissing().contains(actualFile)) {
                    this.log.debug("For any reason file is missing and situation have to be handeld...");
                    File thisFile = new File(this.repositoryPath, string);
                    if (!thisFile.isFile()) {
                        this.index.remove(this.repositoryPath, thisFile);
                        this.index.write();
                        continue;
                    } else {
                        if (idxEntry.update(thisFile)) {
                            this.index.write();
                        }
                    }
                }

                this.log.trace("Adding file to tree...");
                if (idxEntry != null) {
                    projectTree.addFile(string);
                    TreeEntry newMember = projectTree.findBlobMember(string);

                    newMember.setId(idxEntry.getObjectId());
                }
            }
            this.log.debug("Writing tree with subtrees...");
            writeTreeWithSubTrees(projectTree);

            this.log.trace("Retrieving current ids for commit...");
            ObjectId currentHeadId = this.repository.resolve(Constants.HEAD);
            ObjectId[] parentIds;
            if (currentHeadId != null) {
                parentIds = new ObjectId[] { currentHeadId };
            } else {
                parentIds = new ObjectId[0];
            }

            this.log.trace("Creating commit object and prepare for commit...");
            org.eclipse.jgit.lib.Commit commit = new org.eclipse.jgit.lib.Commit(this.repository, parentIds);
            commit.setTree(projectTree);
            commit.setMessage(this.commitMessage);
            commit.setAuthor(new PersonIdent(this.authorName, this.authorEmail));
            commit.setCommitter(new PersonIdent(this.commiterName, this.commiterEmail));

            this.log.debug("Writing commit to disk...");
            ObjectWriter writer = new ObjectWriter(this.repository);
            commit.setCommitId(writer.writeCommit(commit));
            RefUpdate ru = this.repository.updateRef(Constants.HEAD);
            ru.setNewObjectId(commit.getCommitId());
            ru.setRefLogMessage(this.commitMessage, true);
            if (ru.forceUpdate() == RefUpdate.Result.LOCK_FAILURE) {
                throw new RepositoryStateException("Failed to update " + ru.getName() + " to commit "
                        + commit.getCommitId() + ".");
            }

            return commit.getCommitId().name();
        } catch (Exception e) {
            throw new RepositoryStateException("Cant do commit...", e);
        }
    }

    public Commit setAuthor(String fullName, String email) {
        this.authorName = fullName;
        this.authorEmail = email;
        return this;
    }

    public Commit setMessage(String message) {
        this.commitMessage = message;
        return this;
    }

    private void writeTreeWithSubTrees(Tree tree) throws Exception {
        if (tree.getId() == null) {
            this.log.debug("writing tree for: " + tree.getFullName());
            try {
                for (TreeEntry entry : tree.members()) {
                    if (entry.isModified()) {
                        if (entry instanceof Tree) {
                            writeTreeWithSubTrees((Tree) entry);
                        } else {
                            this.log.warn("BAD JUJU: " + entry.getFullName());
                        }
                    }
                }
                ObjectWriter writer = new ObjectWriter(tree.getRepository());
                tree.setId(writer.writeTree(tree));
            } catch (IOException e) {
                throw new Exception("Writing trees", e);
            }
        }
    }
}
