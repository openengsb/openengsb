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

package org.openengsb.scm.common.commands;

import java.io.File;
import java.net.URI;
import java.util.List;

import javax.jbi.management.DeploymentException;

import org.openengsb.drools.model.MergeResult;
import org.openengsb.drools.model.ScmLogEntry;

/**
 * A default implementation of the CommandFactory. It actually implements all
 * the functionality needed for a working CommandFactory except the
 * instantiation of specific classes (which cannot be done here due to lack of
 * information). However, to overcome this, the Template-Pattern is utilized and
 * Command-instantiation is deferred to a sub-class. All create-methods are
 * responsible to set up the Command's Domain-specific parameters accordingly.
 * Utilize the <code>{@link #setScmParameters}</code> method to accomplish this
 * easily.
 * 
 */
public abstract class AbstractScmCommandFactory implements CommandFactory {
    private static final String DEFAULT_WORKING_COPY_NAME = "workingCopy";
    private static final String DEFAULT_ENGSB_WORKING_COPY = "./data/openengsb";

    private String workingCopy;
    private File workingCopyFile;

    private String connection; // expected to be read only
    private URI connectionUri;

    private String developerConnection; // expected to have full access
    private URI developerConnectionUri;

    protected String username;
    protected String password;

    /* interface implementation */

    /**
     * Used to validate the Commandfactory's parameters. Override this method to
     * perform additional validations, but be sure to call super ;)
     */
    @Override
    public void validate() throws DeploymentException {
        try {
            URI connection = getConnectionUri();
            URI developerConnection = getDeveloperConnectionUri();

            if ((connection == null) && (developerConnection == null)) {
                throw new DeploymentException("Neither connection nor developerConnection was set!");
            }
        } catch (IllegalArgumentException exception) {
            throw new DeploymentException(exception);
        }
    }

    @Override
    public Command<Object> getAddCommand(String file) {
        // create command
        AddCommand command = createAddCommand();

        // set up command
        command.setFileToAdd(file);

        return command;
    }

    @Override
    public Command<String> getBlameCommand(String file) {
        return getBlameCommand(file, null);
    }

    @Override
    public Command<String> getBlameCommand(String file, String revision) {
        // create command
        BlameCommand command = createBlameCommand();

        // set up command
        command.setFile(file);
        command.setRevision(revision);

        return command;
    }

    @Override
    public Command<Object> getBranchCommand(String branchName, String commitMessage) {
        // create command
        BranchCommand command = createBranchCommand();

        // set up command
        command.setBranchName(branchName);
        command.setCommitMessage(commitMessage);

        return command;
    }

    @Override
    public Command<MergeResult> getCheckoutCommand(String author) {
        // create command
        CheckoutCommand command = createCheckoutCommand();

        // set up command
        command.setAuthor(author);

        return command;
    }

    @Override
    public Command<MergeResult> getCheckoutOrUpdateCommand(String author) {
        Command<MergeResult> command;

        if (getWorkingCopyFile().exists())
            command = getUpdateCommand();
        else
            command = getCheckoutCommand(author);

        return command;
    }

    @Override
    public Command<MergeResult> getCommitCommand(String author, String message) {
        return getCommitCommand(author, message, null);
    }

    @Override
    public Command<MergeResult> getCommitCommand(String author, String message, String subPath) {
        // create command
        CommitCommand command = createCommitCommand();

        // set up command
        command.setAuthor(author);
        command.setMessage(message);
        command.setSubPath(subPath);

        return command;
    }

    @Override
    public Command<Object> getDeleteCommand(String file) {
        // create command
        DeleteCommand command = createDeleteCommand();

        // set up command
        command.setFile(file);

        return command;
    }

    @Override
    public Command<String> getDiffCommand(String file, String revision) {
        return getDiffCommand(file, file, revision, "HEAD");
    }

    @Override
    public Command<String> getDiffCommand(String oldFile, String newFile, String oldRevision, String newRevision) {
        // create command
        DiffCommand command = createDiffCommand();

        // set up command
        command.setOldFile(oldFile);
        command.setNewFile(newFile);
        command.setOldRevision(oldRevision);
        command.setNewRevision(newRevision);

        return command;
    }

    @Override
    public Command<Object> getExportCommand(String exportDestinationPath) {
        // create command
        ExportCommand command = createExportCommand();

        // set up command
        command.setExportDestinationPath(exportDestinationPath);

        return command;
    }

    @Override
    public Command<MergeResult> getImportCommand(String importSourcePath, String importDestinationPath,
            String commitMessage, String author) {
        // create command
        ImportCommand command = createImportCommand();

        // set up command
        command.setImportSourcePath(importSourcePath);
        command.setImportDestinationPath(importDestinationPath);
        command.setCommitMessage(commitMessage);
        command.setAuthor(author);

        return command;
    }

    @Override
    public Command<List<String>> getListBranchesCommand() {
        // create command
        ListBranchesCommand command = createListBranchesCommand();

        return command;
    }

    @Override
    public Command<List<ScmLogEntry>> getLogCommand(List<String> files, String startRevision, String endRevision) {
        // create command
        LogCommand command = createLogCommand();

        // set up command
        command.setFiles(files);
        command.setStartRevision(startRevision);
        command.setEndRevision(endRevision);

        return command;
    }

    @Override
    public Command<MergeResult> getMergeCommand(String branchName) {
        // create command
        MergeCommand command = createMergeCommand();

        // set up command
        command.setBranchName(branchName);

        return command;
    }

    @Override
    public Command<Object> getSwitchBranchCommand(String branchName) {
        // create command
        SwitchBranchCommand command = createSwitchBranchCommand();

        // set up command
        command.setBranchName(branchName);

        return command;
    }

    @Override
    public Command<MergeResult> getUpdateCommand() {
        return getUpdateCommand(null);
    }

    @Override
    public Command<MergeResult> getUpdateCommand(String updatePath) {
        // create command
        UpdateCommand command = createUpdateCommand();

        // set up command
        command.setUpdatePath(updatePath);

        return command;
    }

    /* end interface implementation */

    /* getters and setters */

    public void setWorkingCopy(String workingCopy) {
        this.workingCopy = workingCopy;
    }

    public String getWorkingCopy() {
        return this.workingCopy;
    }

    public String getConnection() {
        return this.connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getDeveloperConnection() {
        return this.developerConnection;
    }

    public void setDeveloperConnection(String developerConnection) {
        this.developerConnection = developerConnection;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /* end getters and setters */

    /* helpers */

    private File getWorkingCopyFile() {
        if (this.workingCopyFile == null) {
            this.workingCopyFile = calculateWorkingCopyFile();
        }

        return this.workingCopyFile;
    }

    private File calculateWorkingCopyFile() {
        File file = null;

        if (this.workingCopy != null) {
            file = new File(this.workingCopy);
            if (!file.isAbsolute()) {
                // to the default engsb-direcotry
                file = new File(AbstractScmCommandFactory.DEFAULT_ENGSB_WORKING_COPY, this.workingCopy);
            }
        } else // working-copy was not set -> fall back to default
        {
            file = new File(AbstractScmCommandFactory.DEFAULT_ENGSB_WORKING_COPY,
                    AbstractScmCommandFactory.DEFAULT_WORKING_COPY_NAME);
        }

        return file;
    }

    private URI getConnectionUri() {
        if ((this.connectionUri == null) && (this.connection != null)) {
            this.connectionUri = URI.create(this.connection);
        }

        return this.connectionUri;
    }

    private URI getDeveloperConnectionUri() {
        if ((this.developerConnectionUri == null) && (this.developerConnection != null)) {
            this.developerConnectionUri = URI.create(this.developerConnection);
        }

        return this.developerConnectionUri;
    }

    /**
     * This method should be called in each create-method to set scm-domain
     * specific parameters easily.
     * 
     * @param command The Command to set the parameters for.
     */
    protected void setScmParameters(AbstractScmCommand<?> command) {
        command.setWorkingCopy(getWorkingCopyFile());
        command.setConnection(getConnectionUri());
        command.setDeveloperConnection(getDeveloperConnectionUri());
    }

    /* end helpers */

    /* template methods */

    protected abstract AddCommand createAddCommand();

    protected abstract BlameCommand createBlameCommand();

    protected abstract BranchCommand createBranchCommand();

    protected abstract CheckoutCommand createCheckoutCommand();

    protected abstract CommitCommand createCommitCommand();

    protected abstract DeleteCommand createDeleteCommand();

    protected abstract DiffCommand createDiffCommand();

    protected abstract ExportCommand createExportCommand();

    protected abstract ImportCommand createImportCommand();

    protected abstract ListBranchesCommand createListBranchesCommand();

    protected abstract LogCommand createLogCommand();

    protected abstract MergeCommand createMergeCommand();

    protected abstract SwitchBranchCommand createSwitchBranchCommand();

    protected abstract UpdateCommand createUpdateCommand();

    /* template methods */
}
