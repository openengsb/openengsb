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
package org.openengsb.scm.common.endpoints;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

import org.openengsb.scm.common.commands.CommandFactory;
import org.w3c.dom.Node;


/**
 * Endpoint that can mimic all other Endpoints. It's decision what to do is
 * based on the first XML-Element's name in the in-Message. This comes in handy
 * if you do not want to configure every single Endpoint separately.
 */
public class GeneralScmEndpoint extends AbstractScmEndpoint {
    private static final String BEHAVIOR = "processing general scm-request.";

    private AbstractScmEndpoint addEndpoint = new AddEndpoint();
    private AbstractScmEndpoint blameEndpoint = new BlameEndpoint();
    private AbstractScmEndpoint branchEndpoint = new BranchEndpoint();
    private AbstractScmEndpoint checkoutEndpoint = new CheckoutEndpoint();
    private AbstractScmEndpoint commitEndpoint = new CommitEndpoint();
    private AbstractScmEndpoint deleteEndpoint = new DeleteEndpoint();
    private AbstractScmEndpoint diffEndpoint = new DiffEndpoint();
    private AbstractScmEndpoint exportEndpoint = new ExportEndpoint();
    private AbstractScmEndpoint importEndpoint = new ImportEndpoint();
    private AbstractScmEndpoint listBranchesEndpoint = new ListBranchesEndpoint();
    private AbstractScmEndpoint logEndpoint = new LogEndpoint();
    private AbstractScmEndpoint mergeEndpoint = new MergeEndpoint();
    private AbstractScmEndpoint switchBranchEndpoint = new SwitchBranchEndpoint();
    private AbstractScmEndpoint updateEndpoint = new UpdateEndpoint();

    /**
     * Overrides the templeate-method
     * {@link Abstractendpoint#processInOnlyRequest}. It simply forwards the
     * call to {@link #processInOutRequest} since the workload is the same.
     */
    @Override
    protected void processInOnlyRequest(MessageExchange exchange, NormalizedMessage in) throws Exception {
        // just forward it; we'll distinguish between inOnly and inOut later
        processInOutRequest(exchange, in, null);
    }

    /**
     * Overrides the templeate-method
     * {@link Abstractendpoint#processInOnlyRequest}. The forwarding to the
     * actual Endpoint that does the work is done here, based on the first
     * XML-tag's name in the in-message. The distinction between InOut and
     * InOnly is done by testing whether the out-message is null or not in
     * {@link #processRequest}.
     */
    @Override
    protected void processInOutRequest(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        Node commandElement = extractSingleNode(in, ".");
        if (commandElement == null) {
            throw new IllegalArgumentException("In-Message is empty.");
        }

        String endpointCommandName = commandElement.getNodeName();

        if (EndpointCommandNames.ADD_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Add-Endpoint.");
            processRequest(this.addEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.BLAME_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Blame-Endpoint.");
            processRequest(this.blameEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.BRANCH_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Branch-Endpoint.");
            processRequest(this.branchEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.CHECKOUT_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Checkout-Endpoint.");
            processRequest(this.checkoutEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.COMMIT_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Commit-Endpoint.");
            processRequest(this.commitEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.DELETE_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Delete-Endpoint.");
            processRequest(this.deleteEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.DIFF_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Diff-Endpoint.");
            processRequest(this.diffEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.EXPORT_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Export-Endpoint.");
            processRequest(this.exportEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.IMPORT_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Import-Endpoint.");
            processRequest(this.importEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.LIST_BRANCHES_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to ListBranches-Endpoint.");
            processRequest(this.listBranchesEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.LOG_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Log-Endpoint.");
            processRequest(this.logEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.MERGE_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Merge-Endpoint.");
            processRequest(this.mergeEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.SWITCH_BRANCH_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Switch-Endpoint.");
            processRequest(this.switchBranchEndpoint, exchange, in, out);
        } else if (EndpointCommandNames.UPDATE_COMMAND_NAME.equals(endpointCommandName)) {
            getLog().debug("Forwarding request to Update-Endpoint.");
            processRequest(this.updateEndpoint, exchange, in, out);
        } else {
            getLog().error("No Endpoint found for command " + endpointCommandName + ".");
            throw new UnsupportedOperationException("No Endpoint found for command " + endpointCommandName + ".");
        }
    }

    /**
     * Determines whether to call processInOut or processInOnly on the Endpoint,
     * based on whether out is null or not.
     * 
     * @param endpoint
     * @param exchange
     * @param in
     * @param out
     * @throws Exception
     */
    private void processRequest(AbstractScmEndpoint endpoint, MessageExchange exchange, NormalizedMessage in,
            NormalizedMessage out) throws Exception {
        if (out != null) {
            endpoint.processInOut(exchange, in, out);
        } else {
            endpoint.processInOnly(exchange, in);
        }
    }

    @Override
    protected String getEndpointBehaviour() {
        return GeneralScmEndpoint.BEHAVIOR;
    }

    @Override
    public void setCommandFactory(CommandFactory commandFactory) {
        super.setCommandFactory(commandFactory);

        this.addEndpoint.setCommandFactory(getCommandFactory());
        this.blameEndpoint.setCommandFactory(getCommandFactory());
        this.branchEndpoint.setCommandFactory(getCommandFactory());
        this.checkoutEndpoint.setCommandFactory(getCommandFactory());
        this.commitEndpoint.setCommandFactory(getCommandFactory());
        this.deleteEndpoint.setCommandFactory(getCommandFactory());
        this.diffEndpoint.setCommandFactory(getCommandFactory());
        this.exportEndpoint.setCommandFactory(getCommandFactory());
        this.importEndpoint.setCommandFactory(getCommandFactory());
        this.listBranchesEndpoint.setCommandFactory(getCommandFactory());
        this.logEndpoint.setCommandFactory(getCommandFactory());
        this.mergeEndpoint.setCommandFactory(getCommandFactory());
        this.switchBranchEndpoint.setCommandFactory(getCommandFactory());
        this.updateEndpoint.setCommandFactory(getCommandFactory());
    }

}
