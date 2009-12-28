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

import java.io.File;

import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.DeleteCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNWCClient;


/**
 * A Command that marks a file for deletion from version control Implements the
 * general contracts of <code>{@link Command}</code> and
 * <code>{@link DeleteCommand}</code>.
 */
public class SvnDeleteCommand extends AbstractSvnCommand<Object> implements DeleteCommand {
    private String file = null;

    @Override
    public Object execute() throws ScmException {
        // set up client
        SVNWCClient client = getClientManager().getWCClient();

        // set up parameters
        File fileToDelete = new File(getWorkingCopy(), this.file);
        boolean force = false;
        boolean dryRun = false;

        // sanity checks
        if (!fileToDelete.exists()) {
            throw new ScmException("File " + fileToDelete + " does not exist in working copy.");
        }

        if (!fileToDelete.getAbsolutePath().startsWith(getWorkingCopy().getAbsolutePath())) {
            throw new ScmException("File " + fileToDelete
                    + " left the working copy. Are you trying to do something nasty?");
        }

        // actual call to SVNKit
        try {
            client.doDelete(fileToDelete, force, dryRun);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }

        // dummy null-return
        return null;
    }

    @Override
    public void setFile(String file) {
        this.file = file;
    }

}
