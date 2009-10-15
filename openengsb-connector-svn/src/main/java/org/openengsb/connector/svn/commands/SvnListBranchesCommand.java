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

import java.util.ArrayList;
import java.util.Collection;

import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.ListBranchesCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;


/**
 * A Command that lists all branches' names created so far. Implements the
 * general contracts of <code>{@link Command}</code> and
 * <code>{@link ListBranchesCommand}</code>.
 */
public class SvnListBranchesCommand extends AbstractSvnCommand<String[]> implements ListBranchesCommand {

    @Override
    public String[] execute() throws ScmException {
        // compute branches-url
        SVNURL repositoryUrl = getRepositoryUrl();

        // prepare objects to list the contents of the branches directory
        SVNRepository repository;
        try {
            repository = SVNRepositoryFactory.create(repositoryUrl);
        } catch (SVNException exception) {
            throw new ScmException(exception);
        }
        long revision = -1; // means: do not use revision
        SVNProperties properties = null;
        Collection<?> dirEntries = null;

        // list branches
        Collection<?> branches;
        try {
            branches = repository.getDir(AbstractSvnCommand.BRANCHES, revision, properties, dirEntries);
        } catch (SVNException exception) {
            /*
             * should branches not exist, we do not need to report an error,
             * since branch() would create the branches-directory upon it's
             * first call anyway...
             */
            return new String[0];
        }

        // build string-array
        ArrayList<String> branchesStringList = new ArrayList<String>(branches.size());
        for (Object svnDirEntryObject : branches) {
            // java 1.4 style typecheck and -cast sh*t
            if (svnDirEntryObject instanceof SVNDirEntry) {
                SVNDirEntry entry = (SVNDirEntry) svnDirEntryObject;

                // only add name if the entry is really a directory
                if (entry.getKind() == SVNNodeKind.DIR) {
                    branchesStringList.add(entry.getName());
                }
            }
            // else ignore
        }

        String[] branchesArray = branchesStringList.toArray(new String[branchesStringList.size()]);
        return branchesArray;

    }

}
