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

package org.openengsb.scm.common.commands;

import java.util.Map;

import javax.jbi.management.DeploymentException;

import org.openengsb.scm.common.pojos.MergeResult;


/**
 * Interface all CommandFactories used throughout the SCM-Domain
 * 
 * 
 */
public interface CommandFactory {
    /**
     * Since the factory is configured by the xbean.xml and instantiated by
     * ServiceMix it needs a chance to tell us, that is was misconfigured. Well,
     * this is it.
     * 
     * @throws DeploymentException
     */
    void validate() throws DeploymentException;

    /**
     * Returns a Command that adds a file or folder to version control.
     * 
     * @param file The path to the file or folder to be (recursively) added.
     *        Relative to the working copy's root.
     * @return The appropriate Command.
     */
    Command<Object> getAddCommand(String file);

    /**
     * Returns a Command that annotates each line of a file's content with
     * additional data (revision and author of last modification) and returns
     * the content. This call equals <code>getBlameCommand (file, null);</code>
     * 
     * @param file The path to the file to be blamed. Relative to the working
     *        copy's root.
     * @return The appropriate Command.
     */
    Command<String> getBlameCommand(String file);

    /**
     * Returns a Command that annotates each line of a file's content with
     * additional data (revision and author of last modification) and returns
     * the content.
     * 
     * @param file The path to the file to be blamed. Relative to the working
     *        copy's root.
     * @param revision The file's revision to be annotated; may be null
     * @return The appropriate Command.
     */
    Command<String> getBlameCommand(String file, String revision);

    /**
     * Returns a Command that checks out the remote repository's content into
     * the folder supplied in the SU-configuration.
     * 
     * @param author The author's name/id
     * @return the appropriate Command.
     */
    Command<MergeResult> getCheckoutCommand(String author);

    /**
     * Returns a Command that marks a file for deletion from version control.
     * 
     * @param file The path to the file to be deleted. Relative to the working
     *        copy's root.
     * @return The appropriate Command.
     */
    Command<Object> getDeleteCommand(String file);

    /**
     * Returns a Command that computes and returns the differences of a file
     * between HEAD and a given revision. This call equals to
     * <code>getDiffCommand (file, file, revision, "HEAD")</code>.
     * 
     * @param file The path to the file to compute the diff for. Relative to the
     *        working copy's root.
     * @param revision The revision to base the diff on.
     * @return The appropriate Command.
     */
    Command<String> getDiffCommand(String file, String revision);

    /**
     * Returns a Command that computes and returns the differences between two
     * files with their respective revisions.
     * 
     * @param oldFile The path to the file to base the diff on.
     * @param newFile The path to the file to calculate the differences for.
     * @param oldRevision oldFile's revision.
     * @param newRevision newFile's revision.
     * @return The appropriate Command.
     */
    Command<String> getDiffCommand(String oldFile, String newFile, String oldRevision, String newRevision);

    /**
     * Returns a Command that collects and returns commit messages for all given
     * files in all given revisions. Depending on the underlying implementation
     * this call may, for non-existent files or revisions, throw an ScmException
     * or simply not include entries in the map.
     * 
     * @param files The paths to the files for which the commit messages shall
     *        be collected. The files are considered to be relative to the
     *        working copy's root.
     * @param startRevision The revision to start the collection of
     *        commit-messages with.
     * @param endRevision The revision to end the collection of commit-messages
     *        with.
     * @return The appropriate Command.
     */
    Command<Map<String, String>> getLogCommand(String[] files, String startRevision, String endRevision);

    /**
     * Returns a Command that commits all changes within the working copy and
     * submits them to the remote repository. This call equals
     * <code>getCommitCommand (author, message, null);</code>
     * 
     * @param author The author's name or id
     * @param message The author's commit message
     * @return The appropriate Command.
     */
    Command<MergeResult> getCommitCommand(String author, String message);

    /**
     * Returns a Command that commits all changes within the subPath and submits
     * them to the remote repository.
     * 
     * @param author The author's name or id
     * @param message The author's commit message
     * @param subPath The path to the file/directory that is supposed to be
     *        committed. Relative to the working copy's root. May be null to
     *        indicate, that the whole working copy shall be committed.
     * @return The appropriate Command.
     */
    Command<MergeResult> getCommitCommand(String author, String message, String subPath);

    /**
     * Returns a Command that creates a new branch. A strategy to store the
     * branches and remember their names will have to be implemented in respect
     * to the underlying SCM-implementation.
     * 
     * @param branchName The branch's name
     * @param commitMessage The author's/brancher's commit message.
     * @return The appropriate Comamnd
     */
    Command<Object> getBranchCommand(String branchName, String commitMessage);

    /**
     * Returns a Command that lists all branches' names created so far.
     * 
     * @return The appropriate Command.
     */
    Command<String[]> getListBranchesCommand();

    /**
     * Returns a command that merges the current working copy with a branch.
     * 
     * @param branchName The branch's name that shall be used for merging. The
     *        names that may be used here are the same names that were used in
     *        the branch-Commands and are supplied by the ListBranchesCommand.
     * @return The appropriate Command.
     */
    Command<MergeResult> getMergeCommand(String branchName);

    /**
     * Returns a Command that exports the whole working copy, i.e. copies
     * everything (recursively) within it "as is" without SCM-metadata.
     * 
     * @param exportDestinationPath The path to copy the working copy to.
     * @return The appropriate Command.
     */
    Command<Object> getExportCommand(String exportDestinationPath);

    /**
     * Returns a Command that imports external files (recursively) to the
     * repository.
     * 
     * @param importSourcePath The path where the data to import is located.
     * @param importDestinationPath The path where to import the data to.
     *        Relative to the checked out path. May be null.
     * @param commitMessage The message used for the commit.
     * @param author The author who performed the import.
     * @return The appropriate Command.
     */
    Command<MergeResult> getImportCommand(String importSourcePath, String importDestinationPath, String commitMessage,
            String author);

    /**
     * Returns a Command that switches to another branch.
     * 
     * @param branchName The branch's name that shall be used for merging. the
     *        names that may be used here are the same names that were used in
     *        branch (String branchName) and are supplied by listBranches().
     * @return The appropriate Command.
     */
    Command<Object> getSwitchBranchCommand(String branchName);

    /**
     * Returns a Command that performs an update on the working-copy's root,
     * i.e. fetches changes from the remote repository and applies them to the
     * working copy. This call equals to <code>getUpdateCommand (null);</code>
     * 
     * @return The appropriate Command.
     */
    Command<MergeResult> getUpdateCommand();

    /**
     * Returns a Command that performs an update on updatePath, i.e. fetches
     * changes from the remote repository and applies them to the working copy.
     * 
     * @param updatePath The path to perform the update to. Relative to the
     *        working-copy's root. May be null to indicate that the whole
     *        working copy shall be updated.
     * @return The appropriate Command.
     */
    Command<MergeResult> getUpdateCommand(String updatePath);
}
