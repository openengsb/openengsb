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
package org.openengsb.connector.svn.commands;

import java.io.File;
import java.util.ArrayList;

import org.openengsb.drools.model.MergeResult;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.ImportCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;

/**
 * A Command that imports external files (recursively) to the repository.
 * Implements the general contracts of <code>{@link Command}</code> and
 * <code>{@link ImportCommand}</code>.
 */
public class SvnImportCommand extends AbstractSvnCommand<MergeResult> implements ImportCommand {
    private String importSourcePath = null;
    private String importDestinationPath = null;
    private String commitMessage = null;
    private String author = null;

    @Override
    public MergeResult execute() throws ScmException {
        if (!canWriteToRepository()) {
            throw new ScmException("Must not write to repository (set developerConnection to be able to do so)");
        }

        // set up client
        final ArrayList<String> addedFiles = new ArrayList<String>();
        final ArrayList<String> mergedFiles = new ArrayList<String>();
        final ArrayList<String> deletedFiles = new ArrayList<String>();

        // set up client
        SVNCommitClient client = getClientManager().getCommitClient();
        client.setEventHandler(new EventHandler() {
            @Override
            public void handleEvent(SVNEvent paramSVNEvent, double paramDouble) throws SVNException {
                if (paramSVNEvent.getAction() != null) {
                    int actionId = paramSVNEvent.getAction().getID();

                    if (actionId == SVNEventAction.COMMIT_ADDED.getID()) {
                        addedFiles.add(paramSVNEvent.getFile().getPath());
                    } else if (actionId == SVNEventAction.COMMIT_DELETED.getID()) {
                        deletedFiles.add(paramSVNEvent.getFile().getPath());
                    } else if (actionId == SVNEventAction.COMMIT_MODIFIED.getID()) {
                        mergedFiles.add(paramSVNEvent.getFile().getPath());
                    } else if (actionId == SVNEventAction.COMMIT_REPLACED.getID()) {
                        mergedFiles.add(paramSVNEvent.getFile().getPath());
                        // else do nothing
                    }
                }
            }
        });

        try {
            // set up parameters
            File path = new File(this.importSourcePath);
            SVNURL repositoryUrl = getRepositoryUrlRelativeToWorkigCopy();
            SVNURL destinationUrl;
            if (this.importDestinationPath != null) {
                destinationUrl = repositoryUrl.appendPath(this.importDestinationPath, true);
            } else {
                destinationUrl = repositoryUrl;
            }
            this.commitMessage = this.author + ":\n" + this.commitMessage;
            SVNProperties revisionProperties = new SVNProperties();
            boolean useGlobalIgnores = true;
            boolean ignoreUnknownNodeTypes = true;
            SVNDepth depth = SVNDepth.INFINITY;

            // perform call
            SVNCommitInfo info = client.doImport(path, destinationUrl, this.commitMessage, revisionProperties,
                    useGlobalIgnores, ignoreUnknownNodeTypes, depth);

            // assemble and return MergeResult
            MergeResult result = new MergeResult();
            result.setAdds(addedFiles.toArray(new String[addedFiles.size()]));
            result.setDeletions(deletedFiles.toArray(new String[deletedFiles.size()]));
            result.setMerges(mergedFiles.toArray(new String[mergedFiles.size()]));
            result.setRevision(String.valueOf(info.getNewRevision()));

            // TODO find out how to collect conflicting files...
            // conflicting files are reported in errormessages and therefore
            // should be treated as error in SVN
            if (info.getErrorMessage() != null) {
                throw new ScmException(info.getErrorMessage().getFullMessage());
            } else {
                return result;
            }
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public void setImportSourcePath(String importSourcePath) {
        this.importSourcePath = importSourcePath;
    }

    @Override
    public void setImportDestinationPath(String importDestinationPath) {
        this.importDestinationPath = importDestinationPath;
    }

    @Override
    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    @Override
    public void setAuthor(String author) {
        this.author = author;
    }

}
