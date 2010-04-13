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

import org.openengsb.scm.common.commands.AbstractScmCommandFactory;
import org.openengsb.scm.common.commands.AddCommand;
import org.openengsb.scm.common.commands.BlameCommand;
import org.openengsb.scm.common.commands.BranchCommand;
import org.openengsb.scm.common.commands.CheckoutCommand;
import org.openengsb.scm.common.commands.CommitCommand;
import org.openengsb.scm.common.commands.DeleteCommand;
import org.openengsb.scm.common.commands.DiffCommand;
import org.openengsb.scm.common.commands.ExportCommand;
import org.openengsb.scm.common.commands.ImportCommand;
import org.openengsb.scm.common.commands.ListBranchesCommand;
import org.openengsb.scm.common.commands.LogCommand;
import org.openengsb.scm.common.commands.MergeCommand;
import org.openengsb.scm.common.commands.SwitchBranchCommand;
import org.openengsb.scm.common.commands.UpdateCommand;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;

/**
 * Actual implementation of the AbstractCommandFactory-template. All
 * SVN-specific Command-implementations are instantiated here and filled with
 * SVN-specific parameters. Parameters, that are specific to a certain Command
 * are passed in AbstractCommandFactory.
 */
public class SvnCommandFactory extends AbstractScmCommandFactory {
    private static SVNClientManager clientManager = null;

    /* AbstractCommandFactory implementation */

    @Override
    public AddCommand createAddCommand() {
        SvnAddCommand command = new SvnAddCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public BlameCommand createBlameCommand() {
        SvnBlameCommand command = new SvnBlameCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public BranchCommand createBranchCommand() {
        SvnBranchCommand command = new SvnBranchCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public CheckoutCommand createCheckoutCommand() {
        SvnCheckoutCommand command = new SvnCheckoutCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public CommitCommand createCommitCommand() {
        SvnCommitCommand command = new SvnCommitCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public DeleteCommand createDeleteCommand() {
        // create and set up command
        SvnDeleteCommand command = new SvnDeleteCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public DiffCommand createDiffCommand() {
        SvnDiffCommand command = new SvnDiffCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public ExportCommand createExportCommand() {
        SvnExportCommand command = new SvnExportCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public ImportCommand createImportCommand() {
        SvnImportCommand command = new SvnImportCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public ListBranchesCommand createListBranchesCommand() {
        SvnListBranchesCommand command = new SvnListBranchesCommand(username, password);
        setUpCommand(command);

        return command;
    }

    @Override
    public LogCommand createLogCommand() {
        SvnLogCommand command = new SvnLogCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public MergeCommand createMergeCommand() {
        SvnMergeCommand command = new SvnMergeCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public SwitchBranchCommand createSwitchBranchCommand() {
        SvnSwitchBranchCommand command = new SvnSwitchBranchCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public UpdateCommand createUpdateCommand() {
        SvnUpdateCommand command = new SvnUpdateCommand();
        setUpCommand(command);

        return command;
    }

    /* end AbstractCommandFactory implementation */

    /* helpers */

    /**
     * Initializes the library to work with a repository via different
     * protocols. Copied from {@link http
     * ://svn.svnkit.com/repos/svnkit/tags/1.1.8
     * /doc/examples/src/org/tmatesoft/svn/examples/wc/WorkingCopy.java}
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * ile);
         * 
         * /** Returns a Command that annotates each line of a file's content
         * with additional data (revision and author of last modification) and
         * returns the content. This call equals <code>getBlameCommand (file,
         * null);</code>
         * 
         * @param file The path to the file to be blamed. For using over svn://
         * and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();
        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }

    private void init() {
        setupLibrary();
        if (username != null && password != null) {
            SvnCommandFactory.clientManager = SVNClientManager.newInstance(null, username, password);
        } else {
            SvnCommandFactory.clientManager = SVNClientManager.newInstance();
        }
    }

    private SVNClientManager getClientManager() {
        if (SvnCommandFactory.clientManager == null) {
            init();
        }

        return SvnCommandFactory.clientManager;
    }

    private void setUpCommand(AbstractSvnCommand<?> command) {
        // set scm-specific parameters
        setScmParameters(command);

        // set svn-specific parameters
        command.setClientManager(getClientManager());
    }

    /* end helpers */
}
