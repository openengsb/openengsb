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

import java.io.File;

import org.openengsb.scm.common.commands.AddCommand;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNWCClient;


/**
 * A Command that adds a file or folder to version control. Implements the
 * general contracts of <code>{@link Command}</code> and
 * <code>{@link AddCommand}</code>.
 */
public class SvnAddCommand extends AbstractSvnCommand<Object> implements AddCommand {
    private String fileToAdd = null;

    @Override
    public Object execute() throws ScmException {
        // set up client
        SVNWCClient client = getClientManager().getWCClient();

        // set up parameters
        File fileToAdd = new File(getWorkingCopy(), this.fileToAdd);
        boolean force = false;
        boolean mkdir = fileToAdd.isDirectory();
        boolean climbUnversionedParents = true;
        SVNDepth depth = SVNDepth.INFINITY;
        boolean includeIgnored = false;
        boolean makeParents = fileToAdd.isDirectory();

        // sanity checks
        if (!fileToAdd.exists()) {
            throw new ScmException("File " + fileToAdd + " does not exist in working copy.");
        }

        if (!fileToAdd.getAbsolutePath().startsWith(getWorkingCopy().getAbsolutePath())) {
            throw new ScmException("File " + fileToAdd
                    + " left the working copy. Are you trying to do something nasty?");
        }

        // actual call to SVNKit
        try {
            client.doAdd(fileToAdd, force, mkdir, climbUnversionedParents, depth, includeIgnored, makeParents);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }

        return null;
    }

    @Override
    public void setFileToAdd(String fileToAdd) {
        this.fileToAdd = fileToAdd;
    }

}
