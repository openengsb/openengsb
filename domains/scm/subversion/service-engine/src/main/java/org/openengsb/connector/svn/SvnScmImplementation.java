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
package org.openengsb.connector.svn;

import java.util.List;

import org.openengsb.drools.ScmDomain;
import org.openengsb.drools.model.MergeResult;
import org.openengsb.drools.model.ScmLogEntry;
import org.openengsb.scm.common.commands.CommandFactory;

public class SvnScmImplementation implements ScmDomain {

    private CommandFactory factory;

    public SvnScmImplementation(CommandFactory factory) {
        this.factory = factory;
    }

    @Override
    public void add(String fileToAdd) {
        factory.getAddCommand(fileToAdd).execute();
    }

    @Override
    public String blame(String file, String revision) {
        return factory.getBlameCommand(file, revision).execute();
    }

    @Override
    public void branch(String branchName, String commitMessage) {
        factory.getBranchCommand(branchName, commitMessage).execute();
    }

    @Override
    public MergeResult checkout(String author) {
        return factory.getCheckoutCommand(author).execute();
    }

    @Override
    public MergeResult commit(String author, String commitMessage, String subPath) {
        return factory.getCommitCommand(author, commitMessage, subPath).execute();
    }

    @Override
    public void delete(String file) {
        factory.getDeleteCommand(file).execute();
    }

    @Override
    public String diff(String oldFile, String newFile, String oldRevision, String newRevision) {
        return factory.getDiffCommand(oldFile, newFile, oldRevision, newRevision).execute();
    }

    @Override
    public MergeResult doImport(String sourcePath, String destinationPath, String commitMessage, String author) {
        return factory.getImportCommand(sourcePath, destinationPath, commitMessage, author).execute();
    }

    @Override
    public void export(String destinationPath) {
        factory.getExportCommand(destinationPath).execute();
    }

    @Override
    public List<String> listBranches() {
        return factory.getListBranchesCommand().execute();
    }

    @Override
    public List<ScmLogEntry> log(List<String> files, String startRevision, String endRevision) {
        return factory.getLogCommand(files, startRevision, endRevision).execute();
    }

    @Override
    public MergeResult merge(String branchName) {
        return factory.getMergeCommand(branchName).execute();
    }

    @Override
    public void switchBranch(String branchName) {
        factory.getSwitchBranchCommand(branchName).execute();
    }

    @Override
    public MergeResult update(String updatePath) {
        return factory.getUpdateCommand(updatePath).execute();
    }

}
