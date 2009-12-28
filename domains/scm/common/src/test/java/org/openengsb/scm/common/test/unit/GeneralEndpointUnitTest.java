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
package org.openengsb.scm.common.test.unit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.MessageExchange.Role;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.InOutImpl;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.apache.servicemix.jbi.messaging.RobustInOnlyImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.CommandFactory;
import org.openengsb.scm.common.endpoints.GeneralScmEndpoint;
import org.openengsb.scm.common.exceptions.ScmException;
import org.openengsb.scm.common.pojos.MergeResult;
import org.openengsb.scm.common.test.unit.constants.GeneralEndpointUnitTestConstants;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Unit Test for the {@link GeneralEndpoint}. It simply tests, whether the
 * correct real Endpoint was called (by checking if the corresponding Command
 * was executed) and asserts that calls with an incorrect MEP result in an
 * Exception.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/generalEndpointUnitTestSpring.xml" })
public class GeneralEndpointUnitTest {
    @Resource
    private GeneralEndpointUnitTestConstants CONSTANTS;

    private static Command<Object> addCommand;
    private static Command<String> blameCommand;
    private static Command<Object> branchCommand;
    private static Command<MergeResult> checkoutCommand;
    private static Command<MergeResult> checkoutOrUpdateCommand;
    private static Command<MergeResult> commitCommand;
    private static Command<Object> deleteCommand;
    private static Command<String> diffCommand;
    private static Command<Object> exportCommand;
    private static Command<MergeResult> importCommand;
    private static Command<String[]> listBranchesCommand;
    private static Command<Map<String, String>> logCommand;
    private static Command<MergeResult> mergeCommand;
    private static Command<Object> switchBranchCommand;
    private static Command<MergeResult> updateCommand;

    private static CommandFactory commandFactory;
    private static GeneralScmEndpoint endpoint;

    private boolean setUpAlready = false;

    /* setup */

    @Before
    public void setUpEndpoint() throws Exception {
        // mock commands and factory
        if (this.setUpAlready) {
            return;
        }

        mockCommands();
        mockCommandFactory();

        // set up endpoint
        GeneralEndpointUnitTest.endpoint = new GeneralScmEndpoint() {
            @Override
            protected void send(MessageExchange me) throws MessagingException {
                /*
                 * intentionally left blank to prevent Endpoint from really
                 * sending
                 */
            }

            @Override
            protected void sendSync(MessageExchange me) throws MessagingException {
                /*
                 * intentionally left blank to prevent Endpoint from really
                 * sending
                 */
            }
        };
        GeneralEndpointUnitTest.endpoint.setCommandFactory(GeneralEndpointUnitTest.commandFactory);

        this.setUpAlready = true;
    }

    /* end setup */

    /* creators */

    private MessageExchange createRobustInOnlyMessageExchange(String message) throws MessagingException {
        MessageExchange exchange = mock(RobustInOnlyImpl.class);
        fillExchange(exchange, message);

        return exchange;
    }

    private MessageExchange createInOutMessageExchange(String message) throws MessagingException {
        MessageExchange exchange = mock(InOutImpl.class);
        fillExchange(exchange, message);

        return exchange;
    }

    /* end creators */

    /* tests */

    @Test
    public void add_shouldCallAddCommand() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<add fileToAdd=\""
                + this.CONSTANTS.ADD_FILE + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.addCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void add_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<add fileToAdd=\"" + this.CONSTANTS.ADD_FILE
                + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void blame_shouldCallBlameCommand() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<blame file=\"" + this.CONSTANTS.BLAME_FILE
                + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.blameCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void blame_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<blame file=\""
                + this.CONSTANTS.BLAME_FILE + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void branch_shouldCallBranchCommand() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<branch name=\""
                + this.CONSTANTS.BRANCH_NAME + "\" message=\"" + this.CONSTANTS.BRANCH_COMMIT_MESSAGE + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.branchCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void branch_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<branch name=\"" + this.CONSTANTS.BRANCH_NAME
                + "\" message=\"" + this.CONSTANTS.BRANCH_COMMIT_MESSAGE + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void checkout_shouldCallCheckoutCommand() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<checkout author=\""
                + this.CONSTANTS.CHECKOUT_AUTHOR + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.checkoutCommand).execute();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void checkout_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<checkout author=\""
                + this.CONSTANTS.CHECKOUT_AUTHOR + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }
    
    @Test
    public void checkoutOrUpdate_shouldCallCheckoutOrUpdateCommand() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<checkoutOrUpdate author=\""
                + this.CONSTANTS.CHECKOUT_AUTHOR + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.checkoutOrUpdateCommand).execute();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void checkoutOrUpdate_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<checkoutOrUpdate author=\""
                + this.CONSTANTS.CHECKOUT_AUTHOR + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void commit_shouldCallCommitCommand() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<commit message=\""
                + this.CONSTANTS.COMMIT_MESSAGE + "\" author=\"" + this.CONSTANTS.COMMIT_AUTHOR + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.commitCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void commit_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<commit message=\""
                + this.CONSTANTS.COMMIT_MESSAGE + "\" author=\"" + this.CONSTANTS.COMMIT_AUTHOR + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void delete_shouldCallDeleteCommand() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<delete fileToDelete=\""
                + this.CONSTANTS.DELETE_FILE + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.deleteCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void delete_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<delete fileToDelete=\""
                + this.CONSTANTS.DELETE_FILE + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void diff_shouldCallDiffCommand() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<diff file=\"" + this.CONSTANTS.DIFF_FILE
                + "\" revision=\"" + this.CONSTANTS.DIFF_REVISION + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.diffCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void diff_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<diff file=\"" + this.CONSTANTS.DIFF_FILE
                + "\" revision=\"" + this.CONSTANTS.DIFF_REVISION + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void export_shouldCallExportCommand() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<export path=\""
                + this.CONSTANTS.EXPORT_DESTINATION + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.exportCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void export_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<export path=\""
                + this.CONSTANTS.EXPORT_DESTINATION + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void import_shouldCallImportCommand() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<import source=\"" + this.CONSTANTS.IMPORT_SOURCE
                + "\" message=\"" + this.CONSTANTS.IMPORT_COMMIT_MESSAGE + "\" author=\""
                + this.CONSTANTS.IMPORT_AUTHOR + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.importCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void import_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<import source=\""
                + this.CONSTANTS.IMPORT_SOURCE + "\" message=\"" + this.CONSTANTS.IMPORT_COMMIT_MESSAGE
                + "\" author=\"" + this.CONSTANTS.IMPORT_AUTHOR + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void listBranches_shouldCallListBranchesCommand() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<listBranches/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.listBranchesCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void listBranches_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<listBranches/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void log_shouldCallLogCommand() throws Exception {
        String fileElements = "";
        for (String fileElement : this.CONSTANTS.LOG_FILES) {
            fileElements += ("<element>" + fileElement + "</element>");
        }

        MessageExchange messageExchange = createInOutMessageExchange("<log startRevision=\""
                + this.CONSTANTS.LOG_START_REVISION + "\" endRevision=\"" + this.CONSTANTS.LOG_END_REVISION + "\">"
                + "<files>" + fileElements + "</files>" + "</log>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.logCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void log_shouldFailWhenWrongMepIsUsed() throws Exception {
        String fileElements = "";
        for (String fileElement : this.CONSTANTS.LOG_FILES) {
            fileElements += ("<element>" + fileElement + "</element>");
        }

        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<log startRevision=\""
                + this.CONSTANTS.LOG_START_REVISION + "\" endRevision=\"" + this.CONSTANTS.LOG_END_REVISION + "\">"
                + "<files>" + fileElements + "</files>" + "</log>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void merge_shouldCallMergeCommand() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<merge name=\""
                + this.CONSTANTS.MERGE_BRANCH_NAME + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.mergeCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void merge_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<merge name=\""
                + this.CONSTANTS.MERGE_BRANCH_NAME + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void switchBranch_shouldCallSwitchBranchCommand() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<switchBranch name=\""
                + this.CONSTANTS.SWITCH_BRANCH_BRANCH_NAME + "\"/>");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.switchBranchCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void switchBranch_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<switchBranch name=\""
                + this.CONSTANTS.SWITCH_BRANCH_BRANCH_NAME + "\"/>");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test
    public void update_shouldCallUpdateCommand() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<update />");
        GeneralEndpointUnitTest.endpoint.process(messageExchange);

        verify(GeneralEndpointUnitTest.updateCommand).execute();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void update_shouldFailWhenWrongMepIsUsed() throws Exception {
        MessageExchange messageExchange = createRobustInOnlyMessageExchange("<update />");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void process_shouldFailOnUnknownServiceMethodName() throws Exception {
        MessageExchange messageExchange = createInOutMessageExchange("<unkownServiceMethod />");

        // exception expected here
        GeneralEndpointUnitTest.endpoint.process(messageExchange);
    }

    /* end tests */

    /* helpers */

    private void mockCommands() throws ScmException {
        GeneralEndpointUnitTest.addCommand = mock(AddDummy.class);
        GeneralEndpointUnitTest.blameCommand = mock(BlameDummy.class);
        GeneralEndpointUnitTest.branchCommand = mock(BranchDummy.class);
        GeneralEndpointUnitTest.checkoutCommand = mock(CheckoutDummy.class);
        GeneralEndpointUnitTest.checkoutOrUpdateCommand = mock(CheckoutOrUpdateDummy.class);
        GeneralEndpointUnitTest.commitCommand = mock(CommitDummy.class);
        GeneralEndpointUnitTest.deleteCommand = mock(DeleteDummy.class);
        GeneralEndpointUnitTest.diffCommand = mock(DiffDummy.class);
        GeneralEndpointUnitTest.exportCommand = mock(ExportDummy.class);
        GeneralEndpointUnitTest.importCommand = mock(ImportDummy.class);
        GeneralEndpointUnitTest.listBranchesCommand = mock(ListBranchesDummy.class);
        GeneralEndpointUnitTest.logCommand = mock(LogDummy.class);
        GeneralEndpointUnitTest.mergeCommand = mock(MergeDummy.class);
        GeneralEndpointUnitTest.switchBranchCommand = mock(SwitchBranchDummy.class);
        GeneralEndpointUnitTest.updateCommand = mock(UpdateDummy.class);

        when(GeneralEndpointUnitTest.checkoutCommand.execute()).thenReturn(new MergeResult());
        when(GeneralEndpointUnitTest.checkoutOrUpdateCommand.execute()).thenReturn(new MergeResult());
        when(GeneralEndpointUnitTest.commitCommand.execute()).thenReturn(new MergeResult());
        when(GeneralEndpointUnitTest.importCommand.execute()).thenReturn(new MergeResult());
        when(GeneralEndpointUnitTest.listBranchesCommand.execute()).thenReturn(new String[0]);
        when(GeneralEndpointUnitTest.logCommand.execute()).thenReturn(new HashMap<String, String>(0));
        when(GeneralEndpointUnitTest.mergeCommand.execute()).thenReturn(new MergeResult());
        when(GeneralEndpointUnitTest.updateCommand.execute()).thenReturn(new MergeResult());
    }

    private void mockCommandFactory() throws ScmException {
        GeneralEndpointUnitTest.commandFactory = mock(CommandFactory.class);
        when(GeneralEndpointUnitTest.commandFactory.getAddCommand(this.CONSTANTS.ADD_FILE)).thenReturn(
                GeneralEndpointUnitTest.addCommand);
        when(GeneralEndpointUnitTest.commandFactory.getBlameCommand(this.CONSTANTS.BLAME_FILE)).thenReturn(
                GeneralEndpointUnitTest.blameCommand);
        when(GeneralEndpointUnitTest.commandFactory.getBlameCommand(this.CONSTANTS.BLAME_FILE, null)).thenReturn(
                GeneralEndpointUnitTest.blameCommand);
        when(
                GeneralEndpointUnitTest.commandFactory.getBranchCommand(this.CONSTANTS.BRANCH_NAME,
                        this.CONSTANTS.BRANCH_COMMIT_MESSAGE)).thenReturn(GeneralEndpointUnitTest.branchCommand);
        when(GeneralEndpointUnitTest.commandFactory.getCheckoutCommand(this.CONSTANTS.CHECKOUT_AUTHOR)).thenReturn(
                GeneralEndpointUnitTest.checkoutCommand);
        when(GeneralEndpointUnitTest.commandFactory.getCheckoutOrUpdateCommand(this.CONSTANTS.CHECKOUT_AUTHOR)).thenReturn(
                GeneralEndpointUnitTest.checkoutOrUpdateCommand);
        when(
                GeneralEndpointUnitTest.commandFactory.getCommitCommand(this.CONSTANTS.COMMIT_AUTHOR,
                        this.CONSTANTS.COMMIT_MESSAGE)).thenReturn(GeneralEndpointUnitTest.commitCommand);
        when(
                GeneralEndpointUnitTest.commandFactory.getCommitCommand(this.CONSTANTS.COMMIT_AUTHOR,
                        this.CONSTANTS.COMMIT_MESSAGE, null)).thenReturn(GeneralEndpointUnitTest.commitCommand);
        when(GeneralEndpointUnitTest.commandFactory.getDeleteCommand(this.CONSTANTS.DELETE_FILE)).thenReturn(
                GeneralEndpointUnitTest.deleteCommand);
        when(
                GeneralEndpointUnitTest.commandFactory.getDiffCommand(this.CONSTANTS.DIFF_FILE,
                        this.CONSTANTS.DIFF_REVISION)).thenReturn(GeneralEndpointUnitTest.diffCommand);
        when(
                GeneralEndpointUnitTest.commandFactory.getDiffCommand(this.CONSTANTS.DIFF_FILE,
                        this.CONSTANTS.DIFF_FILE, this.CONSTANTS.DIFF_REVISION, "HEAD")).thenReturn(
                GeneralEndpointUnitTest.diffCommand);
        when(GeneralEndpointUnitTest.commandFactory.getExportCommand(this.CONSTANTS.EXPORT_DESTINATION)).thenReturn(
                GeneralEndpointUnitTest.exportCommand);
        when(
                GeneralEndpointUnitTest.commandFactory.getImportCommand(this.CONSTANTS.IMPORT_SOURCE, null,
                        this.CONSTANTS.IMPORT_COMMIT_MESSAGE, this.CONSTANTS.IMPORT_AUTHOR)).thenReturn(
                GeneralEndpointUnitTest.importCommand);
        when(GeneralEndpointUnitTest.commandFactory.getListBranchesCommand()).thenReturn(
                GeneralEndpointUnitTest.listBranchesCommand);
        when(
                GeneralEndpointUnitTest.commandFactory.getLogCommand(this.CONSTANTS.LOG_FILES,
                        this.CONSTANTS.LOG_START_REVISION, this.CONSTANTS.LOG_END_REVISION)).thenReturn(
                GeneralEndpointUnitTest.logCommand);
        when(GeneralEndpointUnitTest.commandFactory.getMergeCommand(this.CONSTANTS.MERGE_BRANCH_NAME)).thenReturn(
                GeneralEndpointUnitTest.mergeCommand);
        when(GeneralEndpointUnitTest.commandFactory.getSwitchBranchCommand(this.CONSTANTS.SWITCH_BRANCH_BRANCH_NAME))
                .thenReturn(GeneralEndpointUnitTest.switchBranchCommand);
        when(GeneralEndpointUnitTest.commandFactory.getUpdateCommand()).thenReturn(
                GeneralEndpointUnitTest.updateCommand);
        when(GeneralEndpointUnitTest.commandFactory.getUpdateCommand(null)).thenReturn(
                GeneralEndpointUnitTest.updateCommand);
    }

    private void fillExchange(MessageExchange exchange, String message) throws MessagingException {
        // create normalized message
        NormalizedMessage normalizedMessage = new NormalizedMessageImpl();
        normalizedMessage.setContent(new StringSource(message));

        // mock troublesome methods
        when(exchange.getRole()).thenReturn(Role.PROVIDER);
        when(exchange.getMessage(MessageExchangeImpl.IN)).thenReturn(normalizedMessage);
        when(exchange.getStatus()).thenReturn(ExchangeStatus.ACTIVE);
        when(exchange.createMessage()).thenReturn(new NormalizedMessageImpl());
    }

    /* end helpers */

    /* dummy classes */

    // needed to get generics right
    private interface AddDummy extends Command<Object> {
    }

    private interface BlameDummy extends Command<String> {
    }

    private interface BranchDummy extends Command<Object> {
    }

    private interface CheckoutDummy extends Command<MergeResult> {
    }
    
    private interface CheckoutOrUpdateDummy extends Command<MergeResult> {
    }

    private interface CommitDummy extends Command<MergeResult> {
    }

    private interface DeleteDummy extends Command<Object> {
    }

    private interface DiffDummy extends Command<String> {
    }

    private interface ExportDummy extends Command<Object> {
    }

    private interface ImportDummy extends Command<MergeResult> {
    }

    private interface ListBranchesDummy extends Command<String[]> {
    }

    private interface LogDummy extends Command<Map<String, String>> {
    }

    private interface MergeDummy extends Command<MergeResult> {
    }

    private interface SwitchBranchDummy extends Command<Object> {
    }

    private interface UpdateDummy extends Command<MergeResult> {
    }

    /* end dummy classes */
}
