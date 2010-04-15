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
package org.openengsb.svn;

import java.io.File;
import java.net.URI;

import org.apache.log4j.Logger;
import org.openengsb.scm.common.ScmConnector;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

public class SvnConnector extends ScmConnector {
    protected static final String BRANCHES = "branches";
    protected static final String TAGS = "tags";
    protected static final String TRUNK = "trunk";

    private Logger log = Logger.getLogger(getClass());

    protected SVNClientManager clientManager;

    public final void init() {
        setupLibrary();

        if (getUsername() != null && getPassword() != null) {
            clientManager = SVNClientManager.newInstance(null, getUsername(), getPassword());
        } else {
            clientManager = SVNClientManager.newInstance();
        }
    }

    public long checkout() {
        URI devCon = getDeveloperConnectionUri();

        try {
            SVNURL svnUrl = SVNURL.create(devCon.getScheme(), devCon.getUserInfo(), devCon.getHost(), devCon.getPort(),
                    devCon.getPath(), true);

            long revision = clientManager.getUpdateClient().doCheckout(svnUrl, getWorkingCopyFile(), SVNRevision.HEAD,
                    SVNRevision.HEAD, SVNDepth.INFINITY, true);

            log.info("Successfully checked out revision " + revision);
            return revision;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    public void switchBranch(String branchName) {
        SVNUpdateClient client = clientManager.getUpdateClient();

        try {
            SVNURL branchUrl;
            if (TRUNK.equalsIgnoreCase(branchName)) {
                branchUrl = getRepositoryUrl().appendPath(TRUNK, true);
            } else {
                SVNURL branchesUrl = getRepositoryUrl().appendPath(BRANCHES, true);
                branchUrl = branchesUrl.appendPath(branchName, true);
            }

            client.doSwitch(getWorkingCopyFile(), branchUrl, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY,
                    false, true);
            log.info("Successfully switched to branch " + branchName);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    public UpdateResult update() {
        final UpdateResult result = new UpdateResult();

        SVNUpdateClient client = clientManager.getUpdateClient();
        client.setEventHandler(new EventHandler() {
            @Override
            public void handleEvent(SVNEvent paramSVNEvent, double paramDouble) throws SVNException {
                if (isRootDirectory(paramSVNEvent.getFile())) {
                    int actionId = paramSVNEvent.getAction().getID();
                    String path = paramSVNEvent.getFile().getPath();

                    if (actionId == SVNEventAction.UPDATE_ADD.getID()) {
                        if (paramSVNEvent.getFile().getParentFile().getName().equalsIgnoreCase(BRANCHES)) {
                            result.getAddedBranches().add(path);
                        } else if (paramSVNEvent.getFile().getParentFile().getName().equalsIgnoreCase(TAGS)) {
                            result.getAddedTags().add(path);
                        }
                    } else if (actionId == SVNEventAction.UPDATE_DELETE.getID()) {
                        if (paramSVNEvent.getFile().getParentFile().getName().equalsIgnoreCase(BRANCHES)) {
                            result.getDeletedBranches().add(path);
                        } else if (paramSVNEvent.getFile().getParentFile().getName().equalsIgnoreCase(TAGS)) {
                            result.getDeletedTags().add(path);
                        }
                    } else if (actionId == SVNEventAction.UPDATE_NONE.getID()) {
                        result.getCommitted().add(path);
                    }
                }
            }
        });

        try {
            long revision = client.doUpdate(getWorkingCopyFile(), SVNRevision.HEAD, SVNDepth.INFINITY, false, false);

            log.info("Successfully updated working copy to revision " + revision);
            return result;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    private boolean isRootDirectory(File file) {
        return file.getName().equalsIgnoreCase(TRUNK) || file.getParentFile().getName().equalsIgnoreCase(BRANCHES)
                || file.getParentFile().getName().equalsIgnoreCase(TAGS);
    }

    /**
     * Initializes the library to work with a repository via different
     * protocols. Copied from {@link http
     * ://svn.svnkit.com/repos/svnkit/tags/1.1.8
     * /doc/examples/src/org/tmatesoft/svn/examples/wc/WorkingCopy.java}
     */
    private void setupLibrary() {
        // For using over http:// and https://
        DAVRepositoryFactory.setup();
        // For using over svn:// and svn+xxx://
        SVNRepositoryFactoryImpl.setup();
        // For using over file:///
        FSRepositoryFactory.setup();
    }

    protected final SVNURL getRepositoryUrl() throws ScmException {
        try {
            SVNRevision revision = null;

            SVNInfo info = clientManager.getWCClient().doInfo(getWorkingCopyFile(), revision);
            SVNURL repositoryUrl = info.getRepositoryRootURL();

            return repositoryUrl;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    protected static abstract class EventHandler implements ISVNEventHandler {
        @Override
        public void checkCancelled() throws SVNCancelException {
            // intentionally left blank
        }
    }

}
