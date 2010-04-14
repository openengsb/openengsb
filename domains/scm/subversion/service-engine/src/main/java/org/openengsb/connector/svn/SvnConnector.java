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
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
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
import org.tmatesoft.svn.core.SVNRevisionProperty;
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
    private static final String HEAD = "HEAD";
    private static final String BRANCHES = "branches";
    private static final String TAGS = "tags";
    private static final String TRUNK = "trunk";

    private Logger log = Logger.getLogger(getClass());

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
        File newFile = new File(getWorkingCopyFile(), fileToAdd);
        if (!newFile.exists()) {
            throw new ScmException("File " + fileToAdd + " does not exist in working copy.");
        }
        if (!newFile.getAbsolutePath().startsWith(getWorkingCopyFile().getAbsolutePath())) {
            throw new ScmException("File " + fileToAdd
                    + " left the working copy. Are you trying to do something nasty?");
        }

        SVNWCClient client = clientManager.getWCClient();

        try {
            client.doAdd(newFile, false, newFile.isDirectory(), true, SVNDepth.INFINITY, false, newFile.isDirectory());
            log.info("File successfully added: " + fileToAdd);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public void branch(String branchName, String commitMessage) {
        if (!canWriteToRepository()) {
            throw new ScmException("Cannot not write to repository (set developerConnection to be able to do so)");
        }
        if (TRUNK.equalsIgnoreCase(branchName)) {
            throw new ScmException(TRUNK + " is not allowed as branch-name");
        }

        SVNCopyClient client = clientManager.getCopyClient();

        try {
            SVNURL trunkUrl = getRepositoryUrl().appendPath(TRUNK, true);
            SVNCopySource[] sources = new SVNCopySource[] { new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD,
                    trunkUrl) };

            SVNURL branchUrl = getRepositoryUrl().appendPath(BRANCHES, true).appendPath(branchName, true);

            client.doCopy(sources, branchUrl, false, true, true, commitMessage, null);
            log.info("Branch successfully created: " + branchName);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public MergeResult checkout(String author) {
        final ArrayList<String> checkedOutFiles = new ArrayList<String>();

        SVNUpdateClient client = clientManager.getUpdateClient();
        client.setEventHandler(new EventHandler() {
            @Override
            public void handleEvent(SVNEvent paramSVNEvent, double paramDouble) throws SVNException {
                if (paramSVNEvent.getAction().getID() == SVNEventAction.UPDATE_ADD.getID()) {
                    checkedOutFiles.add(paramSVNEvent.getFile().getPath());
                }
            }
        });

        URI devCon = getDeveloperConnectionUri();

        try {
            SVNURL svnUrl = SVNURL.create(devCon.getScheme(), devCon.getUserInfo(), devCon.getHost(), devCon.getPort(),
                    devCon.getPath(), true);

            long longRevision = client.doCheckout(svnUrl, getWorkingCopyFile(), SVNRevision.HEAD, SVNRevision.HEAD,
                    SVNDepth.INFINITY, true);

            MergeResult result = new MergeResult();
            result.setAdds(checkedOutFiles);
            result.setRevision("" + longRevision);
            
            log.info("Successfully checked out revision " + longRevision);
            return result;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public MergeResult commit(String author, String commitMessage, String subPath) {
        if (!canWriteToRepository()) {
            throw new ScmException("Cannot not write to repository (set developerConnection to be able to do so)");
        }

        File[] paths = new File[] { getWorkingCopyFile() };
        if (subPath != null && !subPath.isEmpty()) {
            paths[0] = new File(paths[0], subPath);
        }

        SVNProperties properties = new SVNProperties();
        properties.put(SVNRevisionProperty.AUTHOR, author);

        final ArrayList<String> addedFiles = new ArrayList<String>();
        final ArrayList<String> mergedFiles = new ArrayList<String>();
        final ArrayList<String> deletedFiles = new ArrayList<String>();

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
                    }
                }
            }
        });

        try {
            SVNCommitInfo info = client.doCommit(paths, false, commitMessage, properties, new String[0], false, false,
                    SVNDepth.INFINITY);

            MergeResult result = new MergeResult();
            result.setAdds(addedFiles);
            result.setDeletions(deletedFiles);
            result.setMerges(mergedFiles);
            result.setRevision("" + info.getNewRevision());
            
            // TODO find out how to collect conflicting files...
            // conflicting files are reported in errormessages and therefore
            // should be treated as error in SVN
            if (info.getErrorMessage() != null) {
                throw new ScmException(info.getErrorMessage().getFullMessage());
            }
            
            log.info("Successfully committed to revision " + info.getNewRevision());
            return result;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public void delete(String file) {
        File fileToDelete = new File(getWorkingCopyFile(), file);
        if (!fileToDelete.exists()) {
            throw new ScmException("File " + fileToDelete + " does not exist in working copy.");
        }
        if (!fileToDelete.getAbsolutePath().startsWith(getWorkingCopyFile().getAbsolutePath())) {
            throw new ScmException("File " + fileToDelete
                    + " left the working copy. Are you trying to do something nasty?");
        }

        SVNWCClient client = clientManager.getWCClient();

        try {
            client.doDelete(fileToDelete, false, false);
            log.info("Successfully deleted file: " + file);
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
        
        log.info("Successfully listed " + directoriesStringList.size() + " " + (branch ? BRANCHES : TAGS));
        return directoriesStringList;
    }

    @Override
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

    @Override
    public MergeResult update(String updatePath) {
        File path = null;
        if (updatePath == null || HEAD.equalsIgnoreCase(updatePath)) {
            path = getWorkingCopyFile();
        } else {
            path = new File(getWorkingCopyFile(), updatePath);
        }
        
        final ArrayList<String> addedFiles = new ArrayList<String>();
        final ArrayList<String> mergedFiles = new ArrayList<String>();
        final ArrayList<String> deletedFiles = new ArrayList<String>();

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
                    }
                }
            }
        });

        try {
            client.doUpdate(path, SVNRevision.HEAD, SVNDepth.INFINITY, false, false);

            MergeResult result = new MergeResult();
            result.setAdds(addedFiles);
            result.setDeletions(deletedFiles);
            result.setMerges(mergedFiles);

            // TODO find out how to collect conflicts
            
            log.info("Successfully updated: " + updatePath);
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
    private void setupLibrary() {
        // For using over http:// and https://
        DAVRepositoryFactory.setup();
        // For using over svn:// and svn+xxx://
        SVNRepositoryFactoryImpl.setup();
        // For using over file:///
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
