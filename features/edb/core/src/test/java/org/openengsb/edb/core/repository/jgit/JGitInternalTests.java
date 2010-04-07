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

import org.eclipse.jgit.lib.Commit;
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
import org.junit.Before;
import org.junit.Test;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.util.IO;
import org.openengsb.util.Prelude;

/**
 * Internal Tests of JGit showing and testing the behaviour of JGit and for
 * regular regression to test if anything changed in the way jgit works.
 */
@SuppressWarnings("deprecation")
public class JGitInternalTests {

    private Repository repo;
    private String repoPath;
    private File gitDir;

    @Before
    public void setUp() throws Exception {
        StringBuilder pathBuilder = new StringBuilder();

        pathBuilder.append(System.getProperty("user.dir"));
        pathBuilder.append(File.separator).append("target").append(File.separator).append("repo");

        this.repoPath = pathBuilder.toString();

        pathBuilder.append(File.separator).append(".git");

        this.gitDir = new File(pathBuilder.toString());
        // actually create the repo
        this.repo = new Repository(this.gitDir);
        try {
            this.repo.create();
        } catch (IllegalStateException e) {
            if (!e.getMessage().contains("Repository already exists")) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Test
    public void testMultipleCommits() throws Exception {
        testCreateRepositoryAndCheckin();

        GenericContent content = new GenericContent(this.repoPath,
                new String[] { "path1", "path2", "path3", "path4", }, Prelude
                        .dePathize("asdfdf/adfdf/asdfaf/afsdfadfadf"));

        content.store();
        this.repo.getIndex().add(new File(this.repoPath), content.getFileLocation());
        this.repo.getIndex().write();
        testCreateRepositoryAndCheckin();
        content.setProperty("first", "first");
        content.store();
        this.repo.getIndex().add(new File(this.repoPath), content.getFileLocation());
        this.repo.getIndex().write();
        testCreateRepositoryAndCheckin();
        content.setProperty("second", "second");
        content.store();
        this.repo.getIndex().add(new File(this.repoPath), content.getFileLocation());
        this.repo.getIndex().write();
        testCreateRepositoryAndCheckin();
        content.setProperty("third", "third");
        content.store();
        this.repo.getIndex().add(new File(this.repoPath), content.getFileLocation());
        this.repo.getIndex().write();
        testCreateRepositoryAndCheckin();

        IO.deleteStructure(new File(this.repoPath));
    }

    public void testCreateRepositoryAndCheckin() throws Exception {
        IndexDiff diff = new IndexDiff(this.repo.mapTree(Constants.HEAD), this.repo.getIndex());
        diff.diff();

        print(diff.getAdded());
        print(diff.getChanged());
        print(diff.getMissing());
        print(diff.getModified());
        print(diff.getRemoved());

        HashSet<String> allFiles = new HashSet<String>();

        allFiles.addAll(diff.getAdded());
        allFiles.addAll(diff.getChanged());
        allFiles.addAll(diff.getModified());
        allFiles.addAll(diff.getRemoved());

        Tree projectTree = this.repo.mapTree(Constants.HEAD);
        if (projectTree == null) {
            projectTree = new Tree(this.repo);
        }
        GitIndex index = this.repo.getIndex();

        for (String string : allFiles) {
            File actualFile = new File(this.repoPath + File.separator + string);

            TreeEntry treeMember = projectTree.findBlobMember(string);
            if (treeMember != null) {
                treeMember.delete();
            }

            Entry idxEntry = index.getEntry(string);
            if (diff.getMissing().contains(actualFile)) {
                File thisFile = new File(this.repoPath, string);
                if (!thisFile.isFile()) {
                    index.remove(new File(this.repoPath), thisFile);
                    index.write();
                    continue;
                } else {
                    if (idxEntry.update(thisFile)) {
                        index.write();
                    }
                }
            }

            if (idxEntry != null) {
                projectTree.addFile(string);
                TreeEntry newMember = projectTree.findBlobMember(string);

                newMember.setId(idxEntry.getObjectId());
            }
        }
        writeTreeWithSubTrees(projectTree);

        ObjectId currentHeadId = this.repo.resolve(Constants.HEAD);
        ObjectId[] parentIds;
        if (currentHeadId != null) {
            parentIds = new ObjectId[] { currentHeadId };
        } else {
            parentIds = new ObjectId[0];
        }

        Commit commit = new Commit(this.repo, parentIds);
        commit.setTree(projectTree);
        commit.setMessage("blaaa");
        commit.setAuthor(new PersonIdent("me", "anpi@gmx.at"));
        commit.setCommitter(new PersonIdent("me", "anpi@gmx.at"));

        ObjectWriter writer = new ObjectWriter(this.repo);
        commit.setCommitId(writer.writeCommit(commit));
        RefUpdate ru = this.repo.updateRef(Constants.HEAD);
        ru.setNewObjectId(commit.getCommitId());
        ru.setRefLogMessage("blaaa", true);
        if (ru.forceUpdate() == RefUpdate.Result.LOCK_FAILURE) {
            throw new Exception("Failed to update " + ru.getName() + " to commit " + commit.getCommitId() + ".");
        }
    }

    private void print(HashSet<String> toPrint) {
        for (String string : toPrint) {
            System.out.println(string);
        }
    }

    private void writeTreeWithSubTrees(Tree tree) throws Exception {
        if (tree.getId() == null) {
            System.out.println("writing tree for: " + tree.getFullName());
            try {
                for (TreeEntry entry : tree.members()) {
                    if (entry.isModified()) {
                        if (entry instanceof Tree) {
                            writeTreeWithSubTrees((Tree) entry);
                        } else {
                            // this shouldn't happen.... not quite sure what to
                            // do here :)
                            System.out.println("BAD JUJU: " + entry.getFullName());
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
