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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.openengsb.connector.git.domain.GitCommitRef;
import org.openengsb.connector.git.domain.GitTagRef;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.AliveState;
import org.openengsb.domain.scm.CommitRef;
import org.openengsb.domain.scm.ScmDomain;
import org.openengsb.domain.scm.ScmException;
import org.openengsb.domain.scm.TagRef;

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
    public List<CommitRef> update() {
        List<CommitRef> commits = new ArrayList<CommitRef>();
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
                    return null;
                }
                doCheckout(fetchResult);
            } else {
                git.pull().call();
            }
            AnyObjectId newHead = repository.resolve(Constants.HEAD);
            if (newHead == null) {
                return null;
            }
            if (newHead != oldHead) {
                LogCommand logCommand = git.log();
                if (oldHead == null) {
                    logCommand.add(newHead);
                } else {
                    logCommand.addRange(oldHead, newHead);
                }
                Iterable<RevCommit> revisions = logCommand.call();
                for (RevCommit revision : revisions) {
                    commits.add(new GitCommitRef(revision));
                }
            }
        } catch (Exception e) {
            throw new ScmException(e);
        }
        return commits;
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
        if (!checkoutResult)
            throw new IOException("Internal error occured on checking out files");
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
            if (transport != null) {
                transport.close();
            }
        }
    }

    @Override
    public File export() {
        try {
            if (repository == null) {
                initRepository();
            }
            File tmp = File.createTempFile("repository", ".zip");
            ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tmp);
            packRepository(localWorkspace, zos);
            zos.close();
            return tmp;
        } catch (IOException e) {
            throw new ScmException(e);
        }
    }

    @Override
    public File export(CommitRef ref) {
        RevWalk rw = null;
        File tmp = null;
        try {
            if (repository == null) {
                initRepository();
            }

            AnyObjectId headId = repository.resolve(Constants.HEAD);
            AnyObjectId refId = repository.resolve(ref.getStringRepresentation());
            if (headId == null || refId == null) {
                throw new ScmException("Reference doesn't exist.");
            }
            rw = new RevWalk(repository);
            RevCommit head = rw.parseCommit(headId);
            RevCommit commit = rw.parseCommit(refId);

            checkoutIndex(commit);

            tmp = File.createTempFile("repository", ".zip");
            ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tmp);
            packRepository(localWorkspace, zos);
            zos.close();

            checkoutIndex(head);
        } catch (IOException e) {
            throw new ScmException(e);
        } finally {
            if (rw != null) {
                rw.release();
            }
        }
        return tmp;
    }

    private void packRepository(File source, ArchiveOutputStream aos) throws IOException {
        int bufferSize = 2048;
        byte[] readBuffer = new byte[bufferSize];
        int bytesIn = 0;
        File[] files = source.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getName().equals(Constants.DOT_GIT);
            }
        });
        for (File file : files) {
            if (file.isDirectory()) {
                ArchiveEntry ae = aos.createArchiveEntry(file, getRelativePath(file.getAbsolutePath()));
                aos.putArchiveEntry(ae);
                aos.closeArchiveEntry();
                packRepository(file, aos);
            } else {
                FileInputStream fis = new FileInputStream(file);
                ArchiveEntry ae = aos.createArchiveEntry(file, getRelativePath(file.getAbsolutePath()));
                aos.putArchiveEntry(ae);
                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    aos.write(readBuffer, 0, bytesIn);
                }
                aos.closeArchiveEntry();
                fis.close();
            }
        }
    }

    private void checkoutIndex(RevCommit commit) {
        DirCache dc = null;
        try {
            dc = repository.lockDirCache();
            DirCacheCheckout checkout = new DirCacheCheckout(repository, dc, commit.getTree());
            checkout.setFailOnConflict(false);
            checkout.checkout();
        } catch (IOException e) {
            throw new ScmException(e);
        } finally {
            if (dc != null) {
                dc.unlock();
            }
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
            TreeWalk treeWalk = TreeWalk.forPath(repository, arg0, new AnyObjectId[] { commit.getTree() });
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
    public File get(String file) {
        try {
            if (repository == null) {
                initRepository();
            }
            AnyObjectId id = repository.resolve(Constants.HEAD);
            RevCommit commit = new RevWalk(repository).parseCommit(id);
            TreeWalk treeWalk = TreeWalk.forPath(repository, file, new AnyObjectId[] { commit.getTree() });
            if (treeWalk == null) {
                return null;
            }
            ObjectId objectId = treeWalk.getObjectId(treeWalk.getTreeCount() - 1);
            if (objectId == ObjectId.zeroId()) {
                return null;
            }
            String fileName = getFilename(file);
            File tmp = File.createTempFile(fileName, null);
            tmp.deleteOnExit();
            OutputStream os = new FileOutputStream(tmp);
            os.write(repository.open(objectId).getCachedBytes());
            os.close();
            return tmp;
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    @Override
    public boolean exists(String arg0, CommitRef arg1) {
        try {
            AnyObjectId id = repository.resolve(arg1.getStringRepresentation());
            RevCommit commit = new RevWalk(repository).parseCommit(id);
            TreeWalk treeWalk = TreeWalk.forPath(repository, arg0, new AnyObjectId[] { commit.getTree() });
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
    public File get(String file, CommitRef ref) {
        try {
            if (repository == null) {
                initRepository();
            }
            AnyObjectId id = repository.resolve(ref.getStringRepresentation());
            RevCommit commit = new RevWalk(repository).parseCommit(id);
            TreeWalk treeWalk = TreeWalk.forPath(repository, file, new AnyObjectId[] { commit.getTree() });
            if (treeWalk == null) {
                return null;
            }
            ObjectId objectId = treeWalk.getObjectId(treeWalk.getTreeCount() - 1);
            if (objectId == ObjectId.zeroId()) {
                return null;
            }
            String fileName = getFilename(file);
            File tmp = File.createTempFile(fileName, null);
            tmp.deleteOnExit();
            OutputStream os = new FileOutputStream(tmp);
            os.write(repository.open(objectId).getCachedBytes());
            os.close();
            return tmp;
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    private String getFilename(String path) {
        String fileName;
        if (path.contains("/")) {
            fileName = path.substring(path.lastIndexOf("/") + 1);
        } else {
            fileName = path;
        }
        return fileName;
    }

    @Override
    public CommitRef getHead() {
        try {
            if (repository == null) {
                initRepository();
            }
            AnyObjectId id = repository.resolve(Constants.HEAD);
            RevCommit commit = new RevWalk(repository).parseCommit(id);
            return new GitCommitRef(commit);
        } catch (IOException e) {
            if (repository != null) {
                repository.close();
                repository = null;
            }
            throw new ScmException(e);
        }
    }

    @Override
    public CommitRef add(String comment, File... file) {
        if (file.length == 0) {
            return null;
        }
        if (repository == null) {
            prepareWorkspace();
            try {
                initRepository();
            } catch (IOException e) {
                if (repository != null) {
                    repository.close();
                }
                throw new ScmException(e);
            } finally {

            }
        }

        Git git = new Git(repository);
        AddCommand add = git.add();
        try {
            for (File toCommit : file) {
                if (!toCommit.exists()) {
                    throw new ScmException("File " + toCommit + " is not a valid file to commit.");
                }
                String filepattern = getRelativePath(toCommit.getAbsolutePath());
                add.addFilepattern(filepattern);
            }

            add.call();
            return new GitCommitRef(git.commit().setMessage(comment).call());
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    /**
     * Returns the relative path of a file from the working directory of the
     * repository.
     * 
     * @param filePath path of the file within the OS
     * @return path of the file relative to the working directory
     */
    private String getRelativePath(String filePath) throws ScmException {
        final String repoPath = repository.getWorkTree().getAbsolutePath();
        if (filePath.startsWith(repoPath)) {
            return filePath.substring(repoPath.length() + 1);
        } else {
            throw new ScmException("File is not in working directory.");
        }
    }

    @Override
    public CommitRef remove(String comment, File... file) {
        if (file.length == 0) {
            return null;
        }
        if (repository == null) {
            prepareWorkspace();
            try {
                initRepository();
            } catch (IOException e) {
                if (repository != null) {
                    repository.close();
                    repository = null;
                }
                throw new ScmException(e);
            }
        }

        Git git = new Git(repository);
        RmCommand rm = git.rm();
        try {
            for (File toCommit : file) {
                if (!toCommit.exists()) {
                    throw new ScmException("File " + toCommit + " is not a valid file to commit.");
                }
                String filepattern = getRelativePath(toCommit.getAbsolutePath());
                rm.addFilepattern(filepattern);
            }

            rm.call();
            return new GitCommitRef(git.commit().setMessage(comment).call());
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    @Override
    public TagRef tagRepo(String tagName) {
        try {
            if (repository == null) {
                initRepository();
            }
            TagCommand tag = new Git(repository).tag();
            return new GitTagRef(tag.setName(tagName).call());
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    @Override
    public TagRef tagRepo(String tagName, CommitRef ref) {
        try {
            if (repository == null) {
                initRepository();
            }
            AnyObjectId commitRef = repository.resolve(ref.getStringRepresentation());
            if (commitRef == null) {
                return null;
            }
            RevWalk walk = new RevWalk(repository);
            RevCommit revCommit = walk.parseCommit(commitRef);
            TagCommand tag = new Git(repository).tag();
            tag.setName(tagName).setObjectId(revCommit);
            return new GitTagRef(tag.call());
        } catch (Exception e) {
            throw new ScmException(e);
        }
    }

    @Override
    public CommitRef getCommitRefForTag(TagRef ref) {
        try {
            if (repository == null) {
                initRepository();
            }
            AnyObjectId tagRef = repository.resolve(ref.getStringRepresentation());
            if (tagRef == null) {
                return null;
            }
            RevWalk walk = new RevWalk(repository);
            RevTag revTag = walk.parseTag(tagRef);
            CommitRef commitRef = null;
            if (revTag.getObject() instanceof RevCommit) {
                commitRef = new GitCommitRef((RevCommit) revTag.getObject());
            }
            return commitRef;
        } catch (IOException e) {
            throw new ScmException(e);
        }
    }
}
