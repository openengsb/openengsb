/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Collection;

import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.DiffCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNRevision;


/**
 * A Command that computes and returns the differences between two files with
 * their respective revisions. Implements the general contracts of
 * <code>{@link Command}</code> and <code>{@link DiffCommand}</code>.
 */
public class SvnDiffCommand extends AbstractSvnCommand<String> implements DiffCommand {
    private String oldRevision = null;
    private String newRevision = null;
    private String oldFile = null;
    private String newFile = null;

    @Override
    public String execute() throws ScmException {
        if (this.newRevision == null) {
            this.newRevision = AbstractSvnCommand.HEAD_KEYWORD;
        }

        if (this.newFile == null) {
            this.newFile = this.oldFile;
        }

        // set up client
        SVNDiffClient client = getClientManager().getDiffClient();

        // parse revisions
        SVNRevision revision1 = null;
        SVNRevision revision2 = null;

        try {
            if (AbstractSvnCommand.HEAD_KEYWORD.equals(this.oldRevision)) {
                revision1 = SVNRevision.HEAD;
            } else {
                revision1 = SVNRevision.create(Long.parseLong(this.oldRevision));
            }

            if (AbstractSvnCommand.HEAD_KEYWORD.equals(this.newRevision)) {
                revision2 = SVNRevision.HEAD;
            } else {
                revision2 = SVNRevision.create(Long.parseLong(this.newRevision));
            }
        } catch (NumberFormatException exception) {
            throw new ScmException(exception);
        }

        // set up parameters
        File path1 = new File(getWorkingCopy(), this.oldFile);
        File path2 = new File(getWorkingCopy(), this.newFile);
        SVNDepth depth = SVNDepth.INFINITY;
        boolean useAncestry = true;
        OutputStream result = new ByteArrayOutputStream();
        Collection<?> changeLists = null;

        // perform call
        try {
            client.doDiff(path1, revision1, path2, revision2, depth, useAncestry, result, changeLists);
            return result.toString();
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public void setOldRevision(String oldRevision) {
        this.oldRevision = oldRevision;
    }

    @Override
    public void setNewRevision(String newRevision) {
        this.newRevision = newRevision;
    }

    @Override
    public void setOldFile(String oldFile) {
        this.oldFile = oldFile;
    }

    @Override
    public void setNewFile(String newFile) {
        this.newFile = newFile;
    }
}
