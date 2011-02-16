/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.connector.git.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.GitIndex;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.WorkDirCheckout;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.openengsb.connector.git.domain.GitCommitRef;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.AliveState;
import org.openengsb.core.common.DomainMethodNotImplementedException;
import org.openengsb.domain.scm.CommitRef;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.domain.scm.ScmException;

@SuppressWarnings("deprecation")
public class GitServiceImpl extends AbstractOpenEngSBService implements ScmDomain {
    Log log = LogFactory.getLog(GitServiceImpl.class);

    private String remoteLocation;
    private File localWorkspace;
    private String watchBranch;
    private FileRepository repository;

    public GitServiceImpl(String instanceId) {
        super(instanceId);
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.OFFLINE;
    }

    @Override
    public boolean poll() {
        try {
            if (repository == null) {
                prepareWorkspace();
                initRepository();
            }
            Git git = new Git(repository);
            AnyObjectId oldHead = repository.resolve(Constants.HEAD);
            if (oldHead == null) {
                FetchResult fetchResult = doRemoteUpdate();
                if (fetchResult.getTrackingRefUpdate(Constants.R_REMOTES + "origin/" + watchBranch) == null) {
                    return false;
                }
                doCheckout(fetchResult);
            } else {
                PullResult pull = git.pull().call();
                TrackingRefUpdate fetchRef = pull.getFetchResult().getTrackingRefUpdate(
                        Constants.R_REMOTES + "origin/" + watchBranch);
                TrackingRefUpdate mergeRef = pull.getFetchResult().getTrackingRefUpdate(
                        Constants.R_REMOTES + "origin/" + watchBranch);
                if (fetchRef == null && mergeRef == null) {
                    return false;
                }
            }
        } catch (Exception e) {
            if (repository != null) {
                repository.close();
                repository = null;
            }
            throw new ScmException(e);
        }
        return true;
    }

    private void prepareWorkspace() {
        if (localWorkspace == null) {
            throw new ScmException("Local workspace not set.");
        }
        if (!localWorkspace.isDirectory()) {
            tryCreateLocalWorkspace();
        }
    }

    private void tryCreateLocalWorkspace() {
        if (!localWorkspace.exists()) {
            localWorkspace.mkdirs();
        }
        if (!localWorkspace.exists()) {
            throw new ScmException("Local workspace directory '" + localWorkspace
                    + "' does not exist and cannot be created.");
        }
        if (!localWorkspace.isDirectory()) {
            throw new ScmException("Local workspace directory '" + localWorkspace + "' is not a valid directory.");
        }
    }

    protected void doCheckout(FetchResult fetchResult) throws IOException {
        final Ref head = fetchResult.getAdvertisedRef(Constants.R_HEADS + watchBranch);
        final RevWalk rw = new RevWalk(repository);
        final RevCommit mapCommit;
        try {
            mapCommit = rw.parseCommit(head.getObjectId());
        } finally {
            rw.release();
        }

        final RefUpdate u;

        boolean detached = !head.getName().startsWith(Constants.R_HEADS);
        u = repository.updateRef(Constants.HEAD, detached);
        u.setNewObjectId(mapCommit.getId());
        u.forceUpdate();

        DirCacheCheckout dirCacheCheckout = new DirCacheCheckout(repository, null, repository.lockDirCache(), mapCommit
                .getTree());
        dirCacheCheckout.setFailOnConflict(true);
        boolean checkoutResult = dirCacheCheckout.checkout();
        if (!checkoutResult) {
            throw new IOException("Internal error occured on checking out files");
        }
    }

    protected boolean checkoutWatchBranch(FetchResult result) throws IOException {
        Ref head = result.getAdvertisedRef(Constants.R_HEADS + watchBranch);
        if (head == null) {
            return false;
        }

        if (!Constants.HEAD.equals(head.getName())) {
            RefUpdate u = repository.updateRef(Constants.HEAD);
            u.disableRefLog();
            u.link(head.getName());
        }

        final RevWalk rw = new RevWalk(repository);
        final RevCommit commit;
        try {
            commit = rw.parseCommit(head.getObjectId());
        } finally {
            rw.release();
        }
        RefUpdate u = repository.updateRef(Constants.HEAD);
        u.setNewObjectId(commit);
        u.forceUpdate();

        final GitIndex index = new GitIndex(repository);
        final Tree tree = repository.mapTree(commit.getTree());
        final WorkDirCheckout co;

        co = new WorkDirCheckout(repository, repository.getWorkTree(), index, tree);
        co.checkout();
        index.write();
        return true;
    }

    protected FetchResult doRemoteUpdate() throws IOException {
        List<RemoteConfig> remoteConfig = null;
        try {
            remoteConfig = RemoteConfig.getAllRemoteConfigs(repository.getConfig());
        } catch (URISyntaxException e) {
            throw new ScmException(e);
        }
        Transport transport = Transport.open(repository, remoteConfig.get(0));
        try {
            return transport.fetch(NullProgressMonitor.INSTANCE, null);
        } finally {
            transport.close();
        }
    }

    private void initRepository() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setWorkTree(localWorkspace);
        repository = builder.build();
        if (!new File(localWorkspace, ".git").isDirectory()) {
            repository.create();
            repository.getConfig().setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
            repository.getConfig().setString("remote", "origin", "url", remoteLocation);
            repository.getConfig().setString("branch", watchBranch, "remote", "origin");
            repository.getConfig().setString("branch", watchBranch, "merge", "refs/heads/" + watchBranch);
            repository.getConfig().save();
        }
    }

    @Override
    public void export(File directory) {
        try {
            FileUtils.copyDirectory(localWorkspace, directory, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return !pathname.getName().equals(".git");
                }
            });
        } catch (IOException e) {
            throw new ScmException(e);
        }
    }

    public void setRemoteLocation(String remoteLocation) {
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=323571
        if (remoteLocation.startsWith("file:/") && !remoteLocation.startsWith("file:///")) {
            remoteLocation = remoteLocation.replace("file:/", "file:///");
        }
        this.remoteLocation = remoteLocation;
    }

    public void setLocalWorkspace(String localWorkspace) {
        this.localWorkspace = new File(localWorkspace);
    }

    public void setWatchBranch(String watchBranch) {
        this.watchBranch = watchBranch;
    }

    public FileRepository getRepository() {
        if (repository == null) {
            prepareWorkspace();
            try {
                initRepository();
            } catch (IOException e) {
                throw new ScmException(e);
            }
        }
        return repository;
    }

    @Override
    public boolean exists(String arg0) {
        try {
            AnyObjectId id = repository.resolve(Constants.HEAD);
            RevCommit commit = new RevWalk(repository).parseCommit(id);
            TreeWalk treeWalk = TreeWalk.forPath(repository, arg0, new AnyObjectId[]{ commit.getTree() });
            if (treeWalk == null) {
                return false;
            }
            ObjectId objectId = treeWalk.getObjectId(treeWalk.getTreeCount() - 1);
            return !objectId.equals(ObjectId.zeroId());
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    @Override
    public boolean exists(String arg0, CommitRef arg1) {
        try {
            AnyObjectId id = repository.resolve(arg1.getStringRepresentation());
            RevCommit commit = new RevWalk(repository).parseCommit(id);
            TreeWalk treeWalk = TreeWalk.forPath(repository, arg0, new AnyObjectId[]{ commit.getTree() });
            if (treeWalk == null) {
                return false;
            }
            ObjectId objectId = treeWalk.getObjectId(treeWalk.getTreeCount() - 1);
            return !objectId.equals(ObjectId.zeroId());
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    @Override
    public void add(File file) {
        throw new DomainMethodNotImplementedException("Use commit instead.");
    }

    @Override
    public void add(File directory, boolean recursive) {
        throw new DomainMethodNotImplementedException("Use commit instead.");
    }

    @Override
    public CommitRef commit(File file, String comment) {
        if (repository == null) {
            prepareWorkspace();
            try {
                initRepository();
            } catch (IOException e) {
                throw new ScmException(e);
            }
        }
        if (!file.exists() || !file.isFile()) {
            throw new ScmException(file.getName() + " is not a file");
        }
        String repoPath = repository.getWorkTree().getAbsolutePath();
        String filePath = file.getAbsolutePath();
        if (!filePath.startsWith(repoPath)) {
            throw new ScmException("File " + file.getName() + " not within working directory.");
        }
        String filepattern = filePath.substring(repoPath.length() + 1);
        Git git = new Git(repository);
        try {
            git.add().addFilepattern(filepattern).call();
            return new GitCommitRef(git.commit().setMessage(comment).call());
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    @Override
    public CommitRef commit(File directory, String comment, boolean recursive) {
        if (repository == null) {
            prepareWorkspace();
            try {
                initRepository();
            } catch (IOException e) {
                throw new ScmException(e);
            }
        }
        if (!directory.exists() || !directory.isDirectory()) {
            throw new ScmException(directory.getName() + " is not a directory.");
        }
        String repoPath = repository.getWorkTree().getAbsolutePath();
        String filePath = directory.getAbsolutePath();
        if (!filePath.startsWith(repoPath)) {
            throw new ScmException("Directory " + directory.getName() + " not within working directory.");
        }
        String filepattern = filePath.substring(repoPath.length() + 1);
        Git git = new Git(repository);
        AddCommand addCommand = git.add();
        if (!recursive) {
            boolean filesInDirectory = false;
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    filesInDirectory = true;
                    addCommand.addFilepattern(file.getName());
                }
            }
            if (!filesInDirectory) {
                return null;
            }
        } else {
            addCommand.addFilepattern(directory.getAbsolutePath() + "/.");
        }
        try {
            git.add().addFilepattern(filepattern).call();
            return new GitCommitRef(git.commit().setMessage(comment).call());
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    @Override
    public void checkout(String repository, CommitRef version, File directory, boolean recursive) {
        throw new DomainMethodNotImplementedException();
    }

    @Override
    public void checkout(String path, File directory, boolean recursive) {
        throw new DomainMethodNotImplementedException();
    }

}
