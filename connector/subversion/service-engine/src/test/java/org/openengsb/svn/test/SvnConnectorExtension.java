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

package org.openengsb.svn.test;

import java.io.File;

import org.openengsb.scm.common.exceptions.ScmException;
import org.openengsb.svn.SvnConnector;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class SvnConnectorExtension extends SvnConnector {
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
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    public void createDir(String name, String commitMessage, boolean branch) {
        if (!canWriteToRepository()) {
            throw new ScmException("Cannot not write to repository (set developerConnection to be able to do so)");
        }
        if (TRUNK.equalsIgnoreCase(name)) {
            throw new ScmException(TRUNK + " is not allowed as branch-name");
        }

        SVNCopyClient client = clientManager.getCopyClient();

        try {
            SVNURL trunkUrl = getRepositoryUrl().appendPath(TRUNK, true);
            SVNCopySource[] sources = new SVNCopySource[] { new SVNCopySource(SVNRevision.HEAD, SVNRevision.HEAD,
                    trunkUrl) };

            SVNURL branchUrl = getRepositoryUrl().appendPath(branch ? BRANCHES : TAGS, true).appendPath(name, true);

            client.doCopy(sources, branchUrl, false, true, true, commitMessage, null);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    public void commit(String commitMessage, String subPath) {
        if (!canWriteToRepository()) {
            throw new ScmException("Cannot not write to repository (set developerConnection to be able to do so)");
        }

        File[] paths = new File[] { getWorkingCopyFile() };
        if (subPath != null && !subPath.isEmpty()) {
            paths[0] = new File(paths[0], subPath);
        }

        SVNCommitClient client = clientManager.getCommitClient();

        try {
            client.doCommit(paths, false, commitMessage, null, new String[0], false, false, SVNDepth.INFINITY);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

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
            client.doDelete(fileToDelete, false, false, false);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }


}
