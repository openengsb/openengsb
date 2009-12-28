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
package org.openengsb.connector.svn.commands;

import java.io.File;
import java.util.ArrayList;

import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.UpdateCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.openengsb.scm.common.pojos.MergeResult;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;


/**
 * A Command that performs an update on the whole working copy or on a sub-path
 * thereof, i.e. fetches changes from the remote repository and applies them to
 * the working copy. Implements the general contracts of
 * <code>{@link Command}</code> and <code>{@link UpdateCommand}</code>.
 */
public class SvnUpdateCommand extends AbstractSvnCommand<MergeResult> implements UpdateCommand {
    private String updatePath = null;

    @Override
    public MergeResult execute() throws ScmException {
        // set up intermediate lists for result
        final ArrayList<String> addedFiles = new ArrayList<String>();
        final ArrayList<String> mergedFiles = new ArrayList<String>();
        final ArrayList<String> deletedFiles = new ArrayList<String>();

        // set up client
        SVNUpdateClient client = getClientManager().getUpdateClient();
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
        File path = null;
        if ((this.updatePath == null) || (AbstractSvnCommand.HEAD_KEYWORD.equals(this.updatePath))) {
            path = getWorkingCopy();
        } else {
            path = new File(getWorkingCopy(), this.updatePath);
        }

        SVNRevision revision = SVNRevision.HEAD;
        SVNDepth depth = SVNDepth.INFINITY;
        boolean allowUnversionedObstructions = false; // fail when a file shall
        // be added to the WC but
        // an unversioned file
        // already exists
        boolean depthIsSticky = false; // not quite sure what that means; let's
        // leave it off then

        try {
            // perform call
            client.doUpdate(path, revision, depth, allowUnversionedObstructions, depthIsSticky);

            // set up and fill result
            MergeResult result = new MergeResult();
            result.setAdds(addedFiles.toArray(new String[addedFiles.size()]));
            result.setDeletions(deletedFiles.toArray(new String[deletedFiles.size()]));
            result.setMerges(mergedFiles.toArray(new String[mergedFiles.size()]));

            // TODO find out how to collect conflicts
            return result;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public void setUpdatePath(String updatePath) {
        this.updatePath = updatePath;
    }

}
