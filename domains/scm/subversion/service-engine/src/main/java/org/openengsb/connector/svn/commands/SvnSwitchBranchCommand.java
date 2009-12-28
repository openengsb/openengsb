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

import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.SwitchBranchCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;


/**
 * A Command that switches to another branch. Implements the general contracts
 * of <code>{@link Command}</code> and <code>{@link SwitchBranchCommand}</code>.
 */
public class SvnSwitchBranchCommand extends AbstractSvnCommand<Object> implements SwitchBranchCommand {
    private String branchName = null;

    @Override
    public Object execute() throws ScmException {
        // set up client
        SVNUpdateClient client = getClientManager().getUpdateClient();

        try {
            // set up parameters
            SVNURL repositoryUrl = getRepositoryUrl();
            SVNURL branchUrl = null;
            if (AbstractSvnCommand.TRUNK_KEYWORD.equals(this.branchName)) // we
            // want
            // to
            // switch
            // back
            // to
            // trunk
            {
                branchUrl = repositoryUrl.appendPath(AbstractSvnCommand.TRUNK, true);
            } else // we want to switch to branchName
            {
                SVNURL branchesUrl = repositoryUrl.appendPath(AbstractSvnCommand.BRANCHES, true);
                branchUrl = branchesUrl.appendPath(this.branchName, true);
            }

            File path = getWorkingCopy();
            SVNRevision pegRevision = SVNRevision.HEAD;
            SVNRevision revision = SVNRevision.HEAD;
            SVNDepth depth = SVNDepth.INFINITY;
            boolean allowUnversionedObstructions = false;
            boolean depthIsSticky = true;

            // perform call
            client.doSwitch(path, branchUrl, pegRevision, revision, depth, allowUnversionedObstructions, depthIsSticky);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }

        // dummy null-return
        return null;
    }

    @Override
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

}
