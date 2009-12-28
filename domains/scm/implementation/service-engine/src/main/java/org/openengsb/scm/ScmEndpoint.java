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
package org.openengsb.scm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.openengsb.scm.common.endpoints.AbstractEndpoint;
import org.openengsb.scm.exceptions.UnknownIdException;
import org.w3c.dom.Node;


/**
 * @org.apache.xbean.XBean element="scmEndpoint" description="SVN Component" The
 *                         only SCM-Domain-Endpoint. This Endpoint is
 *                         responsible for forwarding all requests to an actual
 *                         connector, based on an id and a lookup-table
 */
public class ScmEndpoint extends AbstractEndpoint {
    private static final String BEHAVIOR = "redirecting request to SCM-implemenation.";
    private static final String NAMESPACE_SERVICE_DELIMITER = ":";

    private static final String ID_XPATH = "./@id";
    private static final String ROOT_XPATH = ".";

    private String lookupTable;
    private Map<String, String> serviceNameTable = null;
    private Map<String, String> namespaceTable = null;

    /* implementation and overrides */

    @Override
    public void validate() throws DeploymentException {
        super.validate();

        try {
            parseLookupTable();
        } catch (FileNotFoundException exception) {
            throw new DeploymentException(exception);
        } catch (IOException exception) {
            throw new DeploymentException(exception);
        }
    }

    @Override
    protected String getEndpointBehaviour() {
        return ScmEndpoint.BEHAVIOR;
    }

    @Override
    protected void processInOnlyRequest(MessageExchange exchange, NormalizedMessage in) throws Exception {
        // simply forward to processInOutRequest; We'll distinguish between
        // InOut and InOnly later, based on the value of the out-message (i.e.
        // null or not-null)
        processInOutRequest(exchange, in, null);
    }

    @Override
    protected void processInOutRequest(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        // 1. get id and message/payload from normalizedMessage
        Node idNode = extractSingleNode(in, ScmEndpoint.ID_XPATH);
        Node rootNode = extractSingleNode(in, ScmEndpoint.ROOT_XPATH);
        Node commandNode = null;
        if (rootNode != null) {
            commandNode = rootNode.getFirstChild();
        }

        String id = null;
        if (idNode != null) {
            id = idNode.getNodeValue();
        }

        if (getLog().isDebugEnabled()) {
            StringBuilder parametersBuilder = new StringBuilder();
            parametersBuilder.append("Parameters:");
            parametersBuilder.append("\nid=");
            parametersBuilder.append(id);
            parametersBuilder.append("\nmessage=");

            // transform message to string
            Transformer messageTransformer = TransformerFactory.newInstance().newTransformer();
            StringWriter stringWriter = new StringWriter();
            messageTransformer.transform(new DOMSource(commandNode), new StreamResult(stringWriter));

            parametersBuilder.append(stringWriter.toString());
        }

        // 2. verify parameters
        if (rootNode == null) {
            getLog().error("Missing in-message.");
            throw new IllegalArgumentException("Missing in-message.");
        }

        if (id == null) {
            getLog().error("Missing id.");
            throw new IllegalArgumentException("Missing id.");
        }

        if (commandNode == null) {
            getLog().error("Missing scm-command.");
            throw new IllegalArgumentException("Missing scm-command.");
        }

        // 3. look up service and namespace from lookuptable
        String serviceName = this.serviceNameTable.get(id);
        String namespace = this.namespaceTable.get(id);

        if (serviceName == null) {
            getLog().error("Could not find mapping for id " + id);
            throw new UnknownIdException("Could not find mapping for id " + id);
        }

        // 4. set up and perform call
        if (out == null) // we are actually processing a inOnly-MEP
        {
            RobustInOnly inOnly = createRobustInOnlyMessage(serviceName, namespace, commandNode);
            getChannel().sendSync(inOnly);
            passOnErrors(exchange, inOnly);
        } else // we are actually processing an InOut-MEP
        {
            InOut inOut = createInOutMessage(serviceName, namespace, commandNode);
            getChannel().sendSync(inOut);
            out.setContent(inOut.getOutMessage().getContent());
            passOnErrors(exchange, inOut);
        }
    }

    /**
     * Method that acts accordingly to the Error-status of the out-Message
     * returned by the connector. This needs to be done in order to notify the
     * callee of errors. By examination of the servicemix-sourcecode, we are
     * required to simple set a fault, but (re-)throw an error, to handle this
     * appropriately.
     * 
     * @param callerExchange The caller's out-message
     * @param calleeExchange The callee's out-message
     * @throws Exception
     */
    private void passOnErrors(MessageExchange callerExchange, MessageExchange calleeExchange) throws Exception {
        if (calleeExchange.getStatus() == ExchangeStatus.ERROR) {
            callerExchange.setStatus(ExchangeStatus.ERROR);
        }

        if (calleeExchange.getFault() != null) {
            callerExchange.setFault(calleeExchange.getFault());
        }

        if (calleeExchange.getError() != null) {
            throw calleeExchange.getError();
        }

    }

    /* end implementation and overrides */

    /**
     * Creates and configures a new Message-Object for the In-Out-MEP
     * 
     * @param service The configured entpoint's name as noted in the SU
     * @param namespace The service's namespace that is used in the SU
     * @param message The actual message as xml-String
     * @return The new and configured InOut-Message-Object
     * @throws MessagingException should something go wrong
     */
    private InOut createInOutMessage(String service, String namespace, Node message) throws MessagingException {
        NormalizedMessage inMessage = new NormalizedMessageImpl();
        inMessage.setContent(new DOMSource(message));

        InOut inOut = getExchangeFactory().createInOutExchange();
        inOut.setService(new QName(namespace, service));
        inOut.setInMessage(inMessage);

        return inOut;
    }

    /**
     * Creates and configures a new Message-Object for the Out-Only-MEP
     * 
     * @param client The client used to create the empty Message-Object
     * @param service The configured entpoint's name as noted in the SU
     * @param namespace The namespace that is used in the SU
     * @param message The actual message as xml-String
     * @return The new and configured In-Only-Message-Object
     * @throws MessagingException should something go wrong
     */
    private RobustInOnly createRobustInOnlyMessage(String service, String namespace, Node message)
            throws MessagingException {
        NormalizedMessage inMessage = new NormalizedMessageImpl();
        inMessage.setContent(new DOMSource(message));

        RobustInOnly robustInOnly = getExchangeFactory().createRobustInOnlyExchange();
        robustInOnly.setService(new QName(namespace, service));
        robustInOnly.setInMessage(inMessage);

        return robustInOnly;
    }

    /* getters and setters */

    public void setLookupTable(String lookupTable) {
        this.lookupTable = lookupTable;
    }

    /* end getters and setters */

    /* helpers */

    /**
     * Parses the lookupTable, defined in the xbean.xml. This Method creates two
     * Maps. One for the serviceName and one for the namespace. Override/Rewrite
     * this mehtod to implement another source for the table.
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void parseLookupTable() throws FileNotFoundException, IOException {
        // get properties from file
        File propertiesFile = new File(this.lookupTable);
        Properties properties = new Properties();

        properties.load(new FileReader(propertiesFile));

        // create maps
        this.serviceNameTable = new HashMap<String, String>(properties.size());
        this.namespaceTable = new HashMap<String, String>(properties.size());

        // fill maps
        for (Object idObject : properties.keySet()) {
            if (idObject instanceof String) {
                String id = (String) idObject;
                String propertyValue = properties.getProperty(id);

                if (propertyValue != null) {
                    setMapValues(id, propertyValue);
                    // else ignore (silently)
                }
            }
            // else ignore
        }
    }

    /**
     * Splits a string on it's last occurrence of ':' and sets the values in the
     * maps
     * 
     * @param value The value to split.
     */
    private void setMapValues(String id, String value) {
        if (value.length() == 0) {
            return;
        }

        // it's quite possible that the namespace also contains a colon...
        // --> only consider last colon
        int lastColonPosition = value.lastIndexOf(ScmEndpoint.NAMESPACE_SERVICE_DELIMITER);

        // split if needed
        String namespace;
        String serviceName;
        if (lastColonPosition < 0) // no colon was found at all; we'll count
        // this as the serviceName then with an
        // omitted namespace
        {
            namespace = "";
            serviceName = value;
        } else // found a colon -> split
        {
            namespace = value.substring(0, lastColonPosition);
            serviceName = value.substring(lastColonPosition + 1, value.length());
        }

        // update tables
        this.namespaceTable.put(id, namespace);
        this.serviceNameTable.put(id, serviceName);
    }

    /* end helpers */
}
