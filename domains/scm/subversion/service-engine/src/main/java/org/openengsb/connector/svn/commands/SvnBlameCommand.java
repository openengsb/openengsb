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
import java.util.Date;

import org.openengsb.scm.common.commands.BlameCommand;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNAnnotateHandler;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;


/**
 * A Command that Annotates each line of a file's content with additional data
 * (revision and author of last modification) and returns the content.
 * Implements the general contracts of <code>{@link Command}</code> and
 * <code>{@link BlameCommand}</code>.
 */
public class SvnBlameCommand extends AbstractSvnCommand<String> implements BlameCommand {
    private String revision = null;
    private String file = null;

    @Override
    public String execute() throws ScmException {
        // determine correct SVNRevision
        SVNRevision svnRevision = determineRevision();

        try {
            // set up client an parameters
            SVNLogClient client = getClientManager().getLogClient();
            File path = new File(getWorkingCopy(), this.file);
            SVNRevision pegRevision = svnRevision;
            SVNRevision startRevision = SVNRevision.create(1);
            SVNRevision endRevision = svnRevision;
            boolean ignoreMimeType = false;
            boolean includeMergedRevisions = true;

            final StringBuilder annotationBuilder = new StringBuilder();
            client.doAnnotate(path, pegRevision, startRevision, endRevision, ignoreMimeType, includeMergedRevisions,
                    new AnnotateHandler() {
                        @Override
                        public void handleLine(Date date, long revision, String author, String line, Date mergedDate,
                                long mergedRevision, String mergedAuthor, String mergedPath, int lineNumber)
                                throws SVNException {
                            annotationBuilder.append(lineNumber + 1);
                            annotationBuilder.append(": ");
                            annotationBuilder.append(line);
                            annotationBuilder.append(" - ");
                            annotationBuilder.append(revision);
                            annotationBuilder.append(" ");
                            annotationBuilder.append(author);
                            annotationBuilder.append(" ");
                            annotationBuilder.append(date);
                            annotationBuilder.append("\n");
                        }
                    }, null);

            return annotationBuilder.toString();
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
    }

    private SVNRevision determineRevision() throws ScmException {
        SVNRevision svnRevision = null;
        if ((this.revision == null) || AbstractSvnCommand.HEAD_KEYWORD.equals(this.revision)) {
            svnRevision = SVNRevision.HEAD;
        } else {
            // parse revision
            int intRevision = -1;
            try {
                intRevision = Integer.parseInt(this.revision);
                if (intRevision <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                throw new ScmException("Revision " + this.revision + " must be a positive integer or "
                        + AbstractSvnCommand.HEAD_KEYWORD);
            }

            svnRevision = SVNRevision.create(intRevision);
        }

        return svnRevision;
    }

    /* end nested classes */

    private static abstract class AnnotateHandler implements ISVNAnnotateHandler {

        @Override
        public void handleEOF() {
            // intentionally ignored
        }

        @Override
        public void handleLine(Date paramDate, long paramLong, String paramString1, String paramString2)
                throws SVNException {
            // intentionally ignored
        }

        @Override
        public boolean handleRevision(Date paramDate, long paramLong, String paramString, File paramFile)
                throws SVNException {
            return false;
        }

    }

    /* end nested classes */

    @Override
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public void setFile(String file) {
        this.file = file;
    }
}
