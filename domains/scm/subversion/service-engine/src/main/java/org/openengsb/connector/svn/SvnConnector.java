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
package org.openengsb.connector.svn;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openengsb.drools.ScmDomain;
import org.openengsb.drools.model.MergeResult;
import org.openengsb.scm.common.ScmConnector;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class SvnConnector extends ScmConnector implements ScmDomain {
    private static final String HEAD_KEYWORD = "HEAD";
    private static final String TRUNK_KEYWORD = "TRUNK";
    private static final String BRANCHES = "branches";
    private static final String TAGS = "tags";
    private static final String TRUNK = "trunk";

    private SVNClientManager clientManager;

    public SvnConnector(SvnConfiguration configuration) {
        setUsername(configuration.getUsername());
        setPassword(configuration.getPassword());
        setWorkingCopy(configuration.getWorkingCopy());
        setDeveloperConnection(configuration.getDeveloperConnection());

        setupLibrary();
        if (getUsername() != null && getPassword() != null) {
            clientManager = SVNClientManager.newInstance(null, getUsername(), getPassword());
        } else {
            clientManager = SVNClientManager.newInstance();
        }
    }

    @Override
    public void add(String fileToAdd) {
        SVNWCClient client = clientManager.getWCClient();

        File newFile = new File(getWorkingCopyFile(), fileToAdd);
        boolean force = false;
        boolean mkdir = newFile.isDirectory();
        boolean climbUnversionedParents = true;
        SVNDepth depth = SVNDepth.INFINITY;
        boolean includeIgnored = false;
        boolean makeParents = newFile.isDirectory();

        // sanity checks
        if (!newFile.exists()) {
            throw new ScmException("File " + fileToAdd + " does not exist in working copy.");
        }

        if (!newFile.getAbsolutePath().startsWith(getWorkingCopyFile().getAbsolutePath())) {
            throw new ScmException("File " + fileToAdd
                    + " left the working copy. Are you trying to do something nasty?");
        }

        // actual call to SVNKit
        try {
            client.doAdd(newFile, force, mkdir, climbUnversionedParents, depth, includeIgnored, makeParents);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public void branch(String branchName, String commitMessage) {
        if (!canWriteToRepository()) {
            throw new ScmException("Must not write to repository (set developerConnection to be able to do so)");
        }

        // check for TRUNK-keyword
        if (TRUNK_KEYWORD.equals(branchName)) {
            throw new ScmException(TRUNK_KEYWORD + " is not allowed as branch-name");
        }

        try {
            // compute trunk and branch-url
            SVNURL repositoryUrl = getRepositoryUrl();
            SVNURL branchesUrl = repositoryUrl.appendPath(BRANCHES, true);
            SVNURL branchUrl = branchesUrl.appendPath(branchName, true);
            SVNURL trunkUrl = repositoryUrl.appendPath(TRUNK, true);

            // set up client and parameters
            SVNCopyClient client = clientManager.getCopyClient();
            SVNRevision revision = SVNRevision.HEAD;
            SVNCopySource[] sources = new SVNCopySource[] { new SVNCopySource(revision, revision, trunkUrl) };
            boolean isMove = false;
            boolean makeParents = true; // let's stay fault tolerant
            boolean failWhenDestinationExists = true; // ... but still not
            // overwrite anything
            // silently
            SVNProperties revisionProperties = null;

            // execute copy
            client.doCopy(sources, branchUrl, isMove, makeParents, failWhenDestinationExists, commitMessage,
                    revisionProperties);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public MergeResult checkout(String author) {
        try {
            // set up parameters
            SVNURL svnUrl = SVNURL.create(getDeveloperConnectionUri().getScheme(), getDeveloperConnectionUri()
                    .getUserInfo(), getDeveloperConnectionUri().getHost(), getDeveloperConnectionUri().getPort(),
                    getDeveloperConnectionUri().getPath(), true);
            SVNRevision revision = SVNRevision.HEAD;
            SVNDepth depth = SVNDepth.INFINITY;

            // set up client
            SVNUpdateClient client = clientManager.getUpdateClient();
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
            long longRevision = client.doCheckout(svnUrl, getWorkingCopyFile(), revision, revision, depth, true);

            MergeResult result = new MergeResult();
            result.setAdds(checkedOutFiles);
            result.setRevision(String.valueOf(longRevision));
            return result;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public MergeResult commit(String author, String commitMessage, String subPath) {
        if (!canWriteToRepository()) {
            throw new ScmException("Must not write to repository (set developerConnection to be able to do so)");
        }

        // set up parameters
        File[] paths = new File[] { getWorkingCopyFile() };
        if (subPath != null && !subPath.isEmpty()) {
            paths[0] = new File(paths[0], subPath);
        }

        boolean keepLocks = false; // we do not use locks anyway, but if
        // something got locked, at least a commit
        // will unlock it
        String message = author + ":\n" + commitMessage;
        SVNProperties properties = new SVNProperties();
        // properties.put (SVNRevisionProperty.AUTHOR, author); darn, cannot add
        // author here :/
        String[] changelists = new String[0]; // should be implemented? hmm
        boolean keepChangelist = false;
        boolean force = false;
        SVNDepth depth = SVNDepth.INFINITY;

        // set up intermediate lists for result
        final ArrayList<String> addedFiles = new ArrayList<String>();
        final ArrayList<String> mergedFiles = new ArrayList<String>();
        final ArrayList<String> deletedFiles = new ArrayList<String>();

        // set up client
        SVNCommitClient client = clientManager.getCommitClient();
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
            // perform call
            SVNCommitInfo info = client.doCommit(paths, keepLocks, message, properties, changelists, keepChangelist,
                    force, depth);

            // set up and fill result
            MergeResult result = new MergeResult();
            result.setAdds(addedFiles);
            result.setDeletions(deletedFiles);
            result.setMerges(mergedFiles);
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
    public void delete(String file) {
        // set up client
        SVNWCClient client = clientManager.getWCClient();

        // set up parameters
        File fileToDelete = new File(getWorkingCopyFile(), file);
        boolean force = false;
        boolean dryRun = false;

        // sanity checks
        if (!fileToDelete.exists()) {
            throw new ScmException("File " + fileToDelete + " does not exist in working copy.");
        }

        if (!fileToDelete.getAbsolutePath().startsWith(getWorkingCopyFile().getAbsolutePath())) {
            throw new ScmException("File " + fileToDelete
                    + " left the working copy. Are you trying to do something nasty?");
        }

        // actual call to SVNKit
        try {
            client.doDelete(fileToDelete, force, dryRun);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public List<String> listBranches() {
        return listDirectories(true);
    }

    @Override
    public List<String> listTags() {
        return listDirectories(false);
    }

    private List<String> listDirectories(boolean branch) {
        SVNRepository repository;
        try {
            repository = SVNRepositoryFactory.create(getRepositoryUrl());
            if (getUsername() != null && getPassword() != null) {
                repository.setAuthenticationManager(new BasicAuthenticationManager(getUsername(), getPassword()));
            }
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }

        Collection<?> directories;
        try {
            directories = repository.getDir(branch ? BRANCHES : TAGS, -1, (SVNProperties) null, (Collection<?>) null);
        } catch (SVNException exception) {
            return new ArrayList<String>();
        }

        ArrayList<String> directoriesStringList = new ArrayList<String>(directories.size());
        for (Object svnDirEntryObject : directories) {
            if (svnDirEntryObject instanceof SVNDirEntry) {
                SVNDirEntry entry = (SVNDirEntry) svnDirEntryObject;

                if (entry.getKind() == SVNNodeKind.DIR) {
                    directoriesStringList.add(entry.getName());
                }
            }
        }

        return directoriesStringList;
    }

    @Override
    public void switchBranch(String branchName) {
        // set up client
        SVNUpdateClient client = clientManager.getUpdateClient();

        try {
            // set up parameters
            SVNURL repositoryUrl = getRepositoryUrl();
            SVNURL branchUrl = null;
            // we want to switch back to trunk
            if (TRUNK_KEYWORD.equals(branchName)) {
                branchUrl = repositoryUrl.appendPath(TRUNK, true);
            } else // we want to switch to branchName
            {
                SVNURL branchesUrl = repositoryUrl.appendPath(BRANCHES, true);
                branchUrl = branchesUrl.appendPath(branchName, true);
            }

            SVNRevision pegRevision = SVNRevision.HEAD;
            SVNRevision revision = SVNRevision.HEAD;
            SVNDepth depth = SVNDepth.INFINITY;
            boolean allowUnversionedObstructions = false;
            boolean depthIsSticky = true;

            // perform call
            client.doSwitch(getWorkingCopyFile(), branchUrl, pegRevision, revision, depth,
                    allowUnversionedObstructions, depthIsSticky);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public MergeResult update(String updatePath) {
        // set up intermediate lists for result
        final ArrayList<String> addedFiles = new ArrayList<String>();
        final ArrayList<String> mergedFiles = new ArrayList<String>();
        final ArrayList<String> deletedFiles = new ArrayList<String>();

        // set up client
        SVNUpdateClient client = clientManager.getUpdateClient();
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
        if (updatePath == null || HEAD_KEYWORD.equals(updatePath)) {
            path = getWorkingCopyFile();
        } else {
            path = new File(getWorkingCopyFile(), updatePath);
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
            result.setAdds(addedFiles);
            result.setDeletions(deletedFiles);
            result.setMerges(mergedFiles);

            // TODO find out how to collect conflicts
            return result;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    /**
     * Initializes the library to work with a repository via different
     * protocols. Copied from {@link http
     * ://svn.svnkit.com/repos/svnkit/tags/1.1.8
     * /doc/examples/src/org/tmatesoft/svn/examples/wc/WorkingCopy.java}
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /**
         * Returns a Command that annotates each line of a file's content with
         * additional data (revision and author of last modification) and
         * returns the content. This call equals <code>getBlameCommand (file,
         * null);</code>
         * 
         * @param file The path to the file to be blamed. For using over svn://
         *        and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();
        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }

    private SVNURL getRepositoryUrl() throws ScmException {
        try {
            SVNRevision revision = null;

            SVNInfo info = this.clientManager.getWCClient().doInfo(getWorkingCopyFile(), revision);
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
