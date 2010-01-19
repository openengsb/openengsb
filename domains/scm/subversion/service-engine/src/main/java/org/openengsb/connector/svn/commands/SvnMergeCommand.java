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
import java.util.Collections;

import org.openengsb.drools.model.MergeResult;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.MergeCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNRevisionRange;

/**
 * A Command that merges the current working copy with a branch. Implements the
 * general contracts of <code>{@link Command}</code> and
 * <code>{@link MergeCommand}</code>.
 */
public class SvnMergeCommand extends AbstractSvnCommand<MergeResult> implements MergeCommand {
    private String branchName = null;

    @Override
    public MergeResult execute() throws ScmException {
        try {
            // set up client
            SVNDiffClient client = getClientManager().getDiffClient();
            // set up intermediate lists for result
            final ArrayList<String> addedFiles = new ArrayList<String>();
            final ArrayList<String> mergedFiles = new ArrayList<String>();
            final ArrayList<String> deletedFiles = new ArrayList<String>();
            client.setEventHandler(new EventHandler() {
                @Override
                public void handleEvent(SVNEvent paramSVNEvent, double paramDouble) throws SVNException {
                    if (paramSVNEvent.getAction() != null) {
                        int actionId = paramSVNEvent.getAction().getID();

                        if (actionId == SVNEventAction.UPDATE_ADD.getID()) {
                            addedFiles.add(paramSVNEvent.getFile().getPath());
                        } else if (actionId == SVNEventAction.UPDATE_DELETE.getID()) {
                            deletedFiles.add(paramSVNEvent.getFile().getPath());
                        } else if (actionId == SVNEventAction.UPDATE_UPDATE.getID()) {
                            mergedFiles.add(paramSVNEvent.getFile().getPath());
                        } else if (actionId == SVNEventAction.UPDATE_REPLACE.getID()) {
                            mergedFiles.add(paramSVNEvent.getFile().getPath());
                            // else do nothing
                        }
                    }
                }
            });

            // set up parameters
            SVNURL repositoryUrl = getRepositoryUrl();
            SVNURL branchesUrl = repositoryUrl.appendPath(AbstractSvnCommand.BRANCHES, true);
            SVNURL branchUrl = branchesUrl.appendPath(this.branchName, true);
            File destinationPath = getWorkingCopy();
            SVNDepth depth = SVNDepth.INFINITY;
            boolean useAncestry = true;
            boolean force = false;
            boolean dryRun = false;
            boolean recordOnly = false;

            SVNRevisionRange rangeToMerge = new SVNRevisionRange(SVNRevision.create(1), SVNRevision.HEAD);

            // perform merge
            client.doMerge(branchUrl, SVNRevision.HEAD, Collections.singleton(rangeToMerge), destinationPath, depth,
                    useAncestry, force, dryRun, recordOnly);

            // assemble mergeResult
            MergeResult result = new MergeResult();
            result.setAdds(addedFiles.toArray(new String[addedFiles.size()]));
            result.setDeletions(deletedFiles.toArray(new String[deletedFiles.size()]));
            result.setMerges(mergedFiles.toArray(new String[mergedFiles.size()]));

            // TODO find out how to collect conflicting files...
            return result;

        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

}
