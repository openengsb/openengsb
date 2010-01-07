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
import java.util.HashMap;
import java.util.Map;

import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.LogCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;


/**
 * A Command that collects and returns commit messages for all given files in
 * all given revisions. Implements the general contracts of
 * <code>{@link Command}</code> and <code>{@link LogCommand}</code>.
 */
public class SvnLogCommand extends AbstractSvnCommand<Map<String, String>> implements LogCommand {
    private String[] files;
    private String startRevision;
    private String endRevision;

    @Override
    public Map<String, String> execute() throws ScmException {
        // set up client
        SVNLogClient logClient = getClientManager().getLogClient();

        // set up parameters
        File[] paths = new File[this.files.length];
        SVNRevision startSvnRevision = null;
        SVNRevision endSvnRevision = null;
        boolean stopOnCopy = false;
        boolean discoverChangedPaths = false;
        long limit = Long.MAX_VALUE; // yes, we want it all. MUAHAHAHA

        final Map<String, String> result = new HashMap<String, String>();
        ISVNLogEntryHandler handler = new ISVNLogEntryHandler() {
            @Override
            public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
                result.put(String.valueOf(logEntry.getRevision()), logEntry.getMessage());
            }
        };

        // additional set up
        for (int i = 0; i < this.files.length; i++) {
            paths[i] = new File(getWorkingCopy(), this.files[i]);
        }

        try {
            if (AbstractSvnCommand.HEAD_KEYWORD.equals(this.startRevision)) {
                startSvnRevision = SVNRevision.HEAD;
            } else {
                startSvnRevision = SVNRevision.create(Long.parseLong(this.startRevision));
            }

            if (AbstractSvnCommand.HEAD_KEYWORD.equals(this.endRevision)) {
                endSvnRevision = SVNRevision.HEAD;
            } else {
                endSvnRevision = SVNRevision.create(Long.parseLong(this.endRevision));
            }
        } catch (NumberFormatException exception) {
            throw new ScmException("Revision mus be positive integer or " + AbstractSvnCommand.HEAD_KEYWORD);
        }

        // perform call
        try {
            logClient.doLog(paths, startSvnRevision, endSvnRevision, stopOnCopy, discoverChangedPaths, limit, handler);
            return result;
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    @Override
    public void setFiles(String[] files) {
        this.files = files;
    }

    @Override
    public void setStartRevision(String startRevision) {
        this.startRevision = startRevision;
    }

    @Override
    public void setEndRevision(String endRevision) {
        this.endRevision = endRevision;
    }

}
