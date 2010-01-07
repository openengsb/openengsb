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

import java.util.ArrayList;

import org.openengsb.scm.common.commands.CheckoutCommand;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.exceptions.ScmException;
import org.openengsb.scm.common.pojos.MergeResult;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;


/**
 * A Command that checks out the remote repository's content into the folder
 * supplied in the SU-configuration. Implements the general contracts of
 * <code>{@link Command}</code> and <code>{@link CheckoutCommand}</code>.
 */
public class SvnCheckoutCommand extends AbstractSvnCommand<MergeResult> implements CheckoutCommand {
    @SuppressWarnings("unused")
    private String author = null; // TODO use

    @Override
    public MergeResult execute() throws ScmException {
        try {
            // set up parameters
            SVNURL svnUrl = SVNURL.create(getRepositoryUri().getScheme(), getRepositoryUri().getUserInfo(),
                    getRepositoryUri().getHost(), getRepositoryUri().getPort(), getRepositoryUri().getPath(), true);
            SVNRevision revision = SVNRevision.HEAD;
            SVNDepth depth = SVNDepth.INFINITY;

            // set up client
            SVNUpdateClient client = getClientManager().getUpdateClient();
            final ArrayList<String> checkedOutFiles = new ArrayList<String>();
            client.setEventHandler(new EventHandler() {
                @Override
                public void handleEvent(SVNEvent paramSVNEvent, double paramDouble) throws SVNException {
                    // check if event means, that a file was added (freshly
                    // checked out)
                    if (paramSVNEvent.getAction().getID() == SVNEventAction.UPDATE_ADD.getID()) {
                        checkedOutFiles.add(paramSVNEvent.getFile().getPath());
                    }
                }

            });

            // call checkout
            long longRevision = client.doCheckout(svnUrl, getWorkingCopy(), revision, revision, depth, true);

            MergeResult result = new MergeResult();
            result.setAdds(checkedOutFiles.toArray(new String[checkedOutFiles.size()]));
            result.setRevision(String.valueOf(longRevision));
            return result;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public void setAuthor(String author) {
        this.author = author;
    }
}
