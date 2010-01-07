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

import org.openengsb.scm.common.commands.BranchCommand;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNRevision;


/**
 * A Command that creates a new branch. A strategy to store the branches and
 * remember their names will have to be implemented in respect to the underlying
 * SCM-implementation. Implements the general contracts of
 * <code>{@link Command}</code> and <code>{@link BranchCommand}</code>.
 */
public class SvnBranchCommand extends AbstractSvnCommand<Object> implements BranchCommand {
    private String branchName = null;
    private String commitMessage = null;

    @Override
    public Object execute() throws ScmException {
        if (!canWriteToRepository()) {
            throw new ScmException("Must not write to repository (set developerConnection to be able to do so)");
        }

        // check for TRUNK-keyword
        if (AbstractSvnCommand.TRUNK_KEYWORD.equals(this.branchName)) {
            throw new ScmException(AbstractSvnCommand.TRUNK_KEYWORD + " is not allowed as branch-name");
        }

        try {
            // compute trunk and branch-url
            SVNURL repositoryUrl = getRepositoryUrl();
            SVNURL branchesUrl = repositoryUrl.appendPath(AbstractSvnCommand.BRANCHES, true);
            SVNURL branchUrl = branchesUrl.appendPath(this.branchName, true);
            SVNURL trunkUrl = repositoryUrl.appendPath(AbstractSvnCommand.TRUNK, true);

            // set up client and parameters
            SVNCopyClient client = getClientManager().getCopyClient();
            SVNRevision revision = SVNRevision.HEAD;
            SVNCopySource[] sources = new SVNCopySource[] { new SVNCopySource(revision, revision, trunkUrl) };
            boolean isMove = false;
            boolean makeParents = true; // let's stay fault tolerant
            boolean failWhenDestinationExists = true; // ... but still not
            // overwrite anything
            // silently
            SVNProperties revisionProperties = null;

            // execute copy
            client.doCopy(sources, branchUrl, isMove, makeParents, failWhenDestinationExists, this.commitMessage,
                    revisionProperties);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }

        // return dummy null-value
        return null;
    }

    @Override
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @Override
    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }
}
