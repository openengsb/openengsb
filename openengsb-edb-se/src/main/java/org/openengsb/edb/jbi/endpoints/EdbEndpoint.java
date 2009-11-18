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
package org.openengsb.edb.jbi.endpoints;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.entities.OperationType;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions.ContentWrapper;
import org.openengsb.edb.jbi.endpoints.XmlParserFunctions.RequestWrapper;

/**
 * @org.apache.xbean.XBean element="edb" The Endpoint to the commit-feature
 * 
 */
public class EdbEndpoint extends AbstractEndpoint {

    /*
     * Operations
     * 
     * Strings to identify an operation. {@link
     * javax.jbi.messaging.MessageExchange} requires a QName as operation.
     * 
     * setOperation(new QName(OPERATION_COMMIT))
     * 
     * The namespace is ignored in the operation-check
     */
    /**
     * String to identify a commit-operation in a
     * {@link javax.jbi.messaging.MessageExchange}
     */
    public static final String OPERATION_COMMIT = "commit";
    /**
     * String to identify a query-operation in a
     * {@link javax.jbi.messaging.MessageExchange}
     */
    public static final String OPERATION_QUERY = "query";
    /**
     * String to identify a reset-operation in a
     * {@link javax.jbi.messaging.MessageExchange}
     */
    public static final String OPERATION_RESET = "reset";

    public static final String DEFAULT_USER = "EDB";
    public static final String DEFAULT_EMAIL = "EDB@engsb.ifs.tuwien.ac.at";

    public static final String COMMIT_OPERATION_TAG_NAME = "operation";
    public static final String QUERY_ELEMENT_NAME = "query";
    public static final int DEFAULT_DEPTH = 1;

    @Override
    protected void processInOutRequest(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws Exception {
        getLog().info("init handler from factory");

        EDBHandler handler = this.factory.loadDefaultRepository();
        getLog().info("parsing message");
        /*
         * Only check the local part. Don't care about the namespace of the
         * operation
         */
        String op = XmlParserFunctions.getMessageType(in);// exchange.getOperation().getLocalPart();
        String body = null;

        if (op.equals(EdbEndpoint.OPERATION_COMMIT))
            try {
                List<ContentWrapper> contentWrappers = XmlParserFunctions.parseCommitMessage(in, handler
                        .getRepositoryBase().toString());
                if (contentWrappers.size() < 1)
                    throw new EDBException("Message did not contain files to commit");
                final List<GenericContent> listAdd = new ArrayList<GenericContent>();
                final List<GenericContent> listRemove = new ArrayList<GenericContent>();

                // EngsbMessage message =
                // EngsbMessage.createFromXml(sourceTransformer
                // .contentToString(in));

                for (final ContentWrapper content : contentWrappers)
                    // update search index
                    if (content.getOperation() == OperationType.UPDATE)
                        listAdd.add(content.getContent());
                    // delete content file
                    else if (content.getOperation() == OperationType.DELETE)
                        listRemove.add(content.getContent());

                handler.add(listAdd);
                handler.remove(listRemove);

                String commitId = handler.commit(EdbEndpoint.DEFAULT_USER, EdbEndpoint.DEFAULT_EMAIL);
                body = XmlParserFunctions.buildCommitBody(contentWrappers, commitId);
            } catch (EDBException e) {
                body = XmlParserFunctions.buildCommitErrorBody(e.getMessage(), makeStackTraceString(e));
                this.logger.info(body);
            }
        else if (op.equals(EdbEndpoint.OPERATION_QUERY)) {
            final List<String> terms = XmlParserFunctions.parseQueryMessage(in);
            List<GenericContent> foundSignals = new ArrayList<GenericContent>();

            try {
                for (final String term : terms) {
                    final List<GenericContent> result = handler.query(term, false);
                    // if (result.size() == 0) {
                    // foundSignals.add(dummy);
                    // } else if (isMergeQ) {
                    // foundSignals.add(result.get(0));
                    // } else {
                    foundSignals.addAll(result);
                    // }
                }

            } catch (final EDBException e) {
                // TODO build error message
                e.printStackTrace();
                foundSignals = new ArrayList<GenericContent>();
            }
            body = XmlParserFunctions.buildQueryBody(foundSignals);
        } else if (op.equals(EdbEndpoint.OPERATION_RESET)) {
            final RequestWrapper req = XmlParserFunctions.parseResetMessage(in);
            // this.log.info(req);
            //
            // final IEDBHandler handler;
            // if (req.getRepoId().equals("")) {
            // handler = this.edbHandlerFactory.loadDefaultRepository();
            // } else {
            // handler = this.edbHandlerFactory.loadRepository(req.getRepoId());
            // }
            try {
                body = XmlParserFunctions.buildResetBody(handler.reset(req.getHeadId(), req.getDepth()));
            } catch (final EDBException e) {
                body = XmlParserFunctions.buildResetErrorBody(e.getMessage(), e.getStackTrace().toString());
            }
        }
        body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><acmResponseMessage><body>" + body
                + "</body></acmResponseMessage>";
        Source response = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        this.logger.info(body);
        out.setContent(response);
        getChannel().send(exchange);
    }

    private String makeStackTraceString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement ste : e.getStackTrace()) {
            sb.append(ste.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    // private Element makeErrorElement(Exception e) {
    // Element result = DocumentHelper.createElement("acmErrorObject");
    // result.addElement("message").setText(e.getMessage());
    // result.addElement("stacktrace").setText(makeStackTraceString(e));
    // return result;
    // }

    /*
     * private Element makeSuccessElement(List<ContentWrapper> persistedSignals,
     * String commitId) { Element result = DocumentHelper.createElement("body");
     * for (final ContentWrapper wrapper : persistedSignals) {
     * 
     * final GenericContent signal = wrapper.getContent();
     * 
     * Element objects = result.addElement("acmMessageObjects");
     * objects.addElement("user", DEFAULT_USER);
     * 
     * 
     * 
     * buildElement("user", DEFAULT_USER, body);
     * buildElement(GenericContent.UUID_NAME, signal.getUUID(), body);
     * buildElement(GenericContent.PATH_NAME, signal.getPath(), body); Element
     * el = result.addElement("acmMessageObject"); for (final Entry<Object,
     * Object> entry : signal .getEntireContent()) {
     * body.append("<acmMessageObject>"); buildElement("key",
     * entry.getKey().toString(), body); buildElement("value",
     * entry.getValue().toString(), body); body.append("</acmMessageObject>"); }
     * 
     * body.append("<operation>").append(wrapper.getOperation())
     * .append("</operation>");
     * 
     * body.append("</acmMessageObjects>"); // TODO implement this return
     * result; }
     */

}
