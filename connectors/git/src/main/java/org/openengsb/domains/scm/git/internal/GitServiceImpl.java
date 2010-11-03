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

package org.openengsb.domains.scm.git.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.GitIndex;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Tree;
import org.eclipse.jgit.lib.WorkDirCheckout;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.Transport;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.domains.scm.ScmDomain;
import org.openengsb.domains.scm.ScmException;

@SuppressWarnings("deprecation")
public class GitServiceImpl implements ScmDomain {

    private String remoteLocation;
    private File localWorkspace;
    private String watchBranch;
    private FileRepository repository;

    public GitServiceImpl() {
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.OFFLINE;
    }

    @Override
    public boolean poll() {
        if (!localWorkspace.isDirectory()) {
            throw new ScmException("local workspace directory does not exist");
        }
        try {
            if (repository == null) {
                initRepository();
            }
            FetchResult result = doRemoteUpdate();
            if (result.getTrackingRefUpdate(Constants.R_REMOTES + "origin/" + watchBranch) == null) {
                return false;
            }
            if (repository.resolve(Constants.R_HEADS + watchBranch) == null) {
                checkoutWatchBranch(result);
                return true;
            }
            ObjectId remote = repository.resolve(Constants.R_REMOTES + "origin/" + watchBranch);
            Git git = new Git(repository);
            MergeCommand merge = git.merge()
                .include("remote", remote)
                .setStrategy(MergeStrategy.OURS);
            merge.call();
        } catch (Exception e) {
            if (repository != null) {
                repository.close();
                repository = null;
            }
            throw new ScmException(e);
        }
        return true;
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

    protected FetchResult doRemoteUpdate() throws URISyntaxException, NotSupportedException, TransportException {
        List<RemoteConfig> remoteConfig = RemoteConfig.getAllRemoteConfigs(repository.getConfig());
        Transport transport = Transport.open(repository, remoteConfig.get(0));
        FetchResult result = transport.fetch(NullProgressMonitor.INSTANCE, null);
        return result;
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
        return repository;
    }
}
