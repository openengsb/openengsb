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
import org.openengsb.scm.common.commands.ExportCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;


/**
 * A Command that exports the whole working copy, i.e. copies everything
 * (recursively) within it "as is" without SCM-metadata. Implements the general
 * contracts of <code>{@link Command}</code> and
 * <code>{@link ExportCommand}</code>.
 */
public class SvnExportCommand extends AbstractSvnCommand<Object> implements ExportCommand {
    private String exportDestinationPath;

    @Override
    public Object execute() throws ScmException {
        // set up client
        SVNUpdateClient client = getClientManager().getUpdateClient();

        // set up parameters
        File srcPath = getWorkingCopy();
        File dstPath = new File(this.exportDestinationPath);
        SVNRevision pegRevision = SVNRevision.HEAD;
        SVNRevision revision = SVNRevision.HEAD;
        String eolStyle = null;
        boolean overwrite = false;
        SVNDepth depth = SVNDepth.INFINITY;

        // perform call
        try {
            client.doExport(srcPath, dstPath, pegRevision, revision, eolStyle, overwrite, depth);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }

        // dummy null-return
        return null;
    }

    @Override
    public void setExportDestinationPath(String exportDestinationPath) {
        this.exportDestinationPath = exportDestinationPath;
    }

}
