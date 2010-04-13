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

import org.openengsb.scm.common.commands.AbstractScmCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * An abstract implementation of the Command-interface to be used by all
 * SVN-Commands. It holds svn-specific members (with their getters and setters),
 * along with some convenience-methods and default implementations of
 * svnKit-specific handlers.
 * 
 * @param <Returntype> The type the command shall return form it's execute-call.
 */
public abstract class AbstractSvnCommand<ReturnType> extends AbstractScmCommand<ReturnType> {
    protected static final String HEAD_KEYWORD = "HEAD";
    protected static final String TRUNK_KEYWORD = "TRUNK";
    protected static final String BRANCHES = "branches";
    protected static final String TRUNK = "trunk";

    private SVNClientManager clientManager = null;

    /* getters and setters */

    protected SVNClientManager getClientManager() {
        return this.clientManager;
    }

    protected void setClientManager(SVNClientManager clientManager) {
        this.clientManager = clientManager;
    }

    /* end getters and setters */

    /* nested classes */

    /**
     * Default-Implementation of ISVNEventHandler. This class implementes
     * <code>checkCancelled()</code> as empty method, since it is not needed in
     * our implementation. <code>checkCancelled()</code> would give us the
     * opportunity to cancel an svn-operation which we do not want in any case.
     */
    protected static abstract class EventHandler implements ISVNEventHandler {
        @Override
        public void checkCancelled() throws SVNCancelException {
            // intentionally left blank
        }
    }

    /* end nested classes */

    /* helpers */

    /**
     * Helper that determines the Repository's URL solely from the working copy
     * 
     * @return The retreived URL
     * @throws ScmException
     */
    protected SVNURL getRepositoryUrl() throws ScmException {
        try {
            SVNRevision revision = null;

            SVNInfo info = this.clientManager.getWCClient().doInfo(getWorkingCopy(), revision);
            SVNURL repositoryUrl = info.getRepositoryRootURL();

            return repositoryUrl;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    /**
     * Helper that determines the Repository's URL honoring the checkout-path.
     * That is, if not the whole repository but a sub-path within it was checked
     * out to create this working-copy, the repository's URL + the subpath is
     * returned
     * 
     * @return The requested URL + subpath
     * @throws ScmException
     */
    protected SVNURL getRepositoryUrlRelativeToWorkigCopy() throws ScmException {
        try {
            SVNRevision revision = null;

            SVNInfo info = this.clientManager.getWCClient().doInfo(getWorkingCopy(), revision);
            SVNURL repositoryUrl = info.getURL();

            return repositoryUrl;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    /* end helpers */

}
