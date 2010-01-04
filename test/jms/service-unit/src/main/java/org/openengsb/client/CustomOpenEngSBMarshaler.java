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
package org.openengsb.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.servicemix.common.JbiConstants;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jms.endpoints.AbstractJmsMarshaler;
import org.apache.servicemix.jms.endpoints.JmsConsumerMarshaler;
import org.apache.servicemix.soap.core.MessageImpl;
import org.apache.servicemix.soap.core.PhaseInterceptorChain;
import org.apache.servicemix.soap.interceptors.mime.AttachmentsInInterceptor;
import org.apache.servicemix.soap.interceptors.mime.AttachmentsOutInterceptor;
import org.apache.servicemix.soap.interceptors.xml.BodyOutInterceptor;
import org.apache.servicemix.soap.interceptors.xml.StaxInInterceptor;
import org.apache.servicemix.soap.interceptors.xml.StaxOutInterceptor;

public class CustomOpenEngSBMarshaler extends AbstractJmsMarshaler implements JmsConsumerMarshaler {

    private URI mep;
    private boolean rollbackOnError;
    private boolean rollbackOnErrorDefault;
    private boolean rollbackConfigured;

    public CustomOpenEngSBMarshaler() {
        this.mep = JbiConstants.IN_ONLY;
    }

    public CustomOpenEngSBMarshaler(URI mep) {
        this.mep = mep;
    }

    /**
     * @return the mep
     */
    public URI getMep() {
        return this.mep;
    }

    /**
     * @param mep the mep to set
     */
    public void setMep(URI mep) {
        this.mep = mep;
    }

    public boolean isRollbackOnError() {
        return this.rollbackConfigured ? this.rollbackOnError : this.rollbackOnErrorDefault;
    }

    /**
     * @param rollbackOnError if exchange in errors should cause a rollback on
     *        the JMS side
     */
    public void setRollbackOnError(boolean rollbackOnError) {
        this.rollbackConfigured = true;
        this.rollbackOnError = rollbackOnError;
    }

    /**
     * This is called to set intelligent defaults if no explicit rollbackOnError
     * configuration is set. If setRollbackOnError is explicitly set, it will be
     * used.
     * 
     * @param rollbackDefault default rollbackOnError setting
     */
    public void setRollbackOnErrorDefault(boolean rollbackDefault) {
        this.rollbackOnErrorDefault = rollbackDefault;
    }

    public JmsContext createContext(Message message) throws Exception {
        return new Context(message);
    }

    public MessageExchange createExchange(JmsContext jmsContext, ComponentContext jbiContext) throws Exception {
        Context ctx = (Context) jmsContext;
        MessageExchange exchange = jbiContext.getDeliveryChannel().createExchangeFactory().createExchange(this.mep);
        NormalizedMessage inMessage = exchange.createMessage();
        populateMessage(ctx.message, inMessage);
        if (isCopyProperties()) {
            copyPropertiesFromJMS(ctx.message, inMessage);
        }

        inMessage.setProperty("contextId", ctx.getMessage().getStringProperty("contextId"));
        exchange.setOperation(new QName(ctx.getMessage().getStringProperty("operation")));
        exchange.setMessage(inMessage, "in");
        return exchange;
    }

    @SuppressWarnings("unchecked")
    public Message createOut(MessageExchange exchange, NormalizedMessage outMsg, Session session, JmsContext context)
            throws Exception {

        if (outMsg == null) {
            return session.createTextMessage("");
        }

        String xmlString = String.valueOf(new SourceTransformer().toString(outMsg.getContent()));
        TextMessage text = session.createTextMessage(xmlString);

        return text;
    }

    @SuppressWarnings("unchecked")
    public Message createFault(MessageExchange exchange, Fault fault, Session session, JmsContext context)
            throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PhaseInterceptorChain chain = new PhaseInterceptorChain();
        chain.add(new AttachmentsOutInterceptor());
        chain.add(new StaxOutInterceptor());
        chain.add(new BodyOutInterceptor());
        org.apache.servicemix.soap.api.Message msg = new MessageImpl();
        msg.setContent(Source.class, fault.getContent());
        msg.setContent(OutputStream.class, baos);
        for (String attId : (Set<String>) fault.getAttachmentNames()) {
            msg.getAttachments().put(attId, fault.getAttachment(attId));
        }
        chain.doIntercept(msg);
        TextMessage text = session.createTextMessage(baos.toString());
        if (msg.get(org.apache.servicemix.soap.api.Message.CONTENT_TYPE) != null) {
            text.setStringProperty(CONTENT_TYPE_PROPERTY, (String) msg
                    .get(org.apache.servicemix.soap.api.Message.CONTENT_TYPE));
        }
        text.setBooleanProperty(FAULT_JMS_PROPERTY, true);
        if (isCopyProperties()) {
            copyPropertiesFromNM(fault, text);
        }
        return text;
    }

    public Message createError(MessageExchange exchange, Exception error, Session session, JmsContext context)
            throws Exception {
        if (this.rollbackOnError) {
            throw error;
        } else {
            ObjectMessage message = session.createObjectMessage(error);
            message.setBooleanProperty(ERROR_JMS_PROPERTY, true);
            return message;
        }
    }

    protected void populateMessage(Message message, NormalizedMessage normalizedMessage) throws Exception {
        if (message instanceof TextMessage) {
            String text = ((TextMessage) message).getText();
            normalizedMessage.setContent(new StringSource(text));
        } else {
            throw new UnsupportedOperationException("JMS message is not a TextMessage");
        }
    }

    protected static class Context implements JmsContext, Serializable {
        Message message;

        Context(Message message) {
            this.message = message;
        }

        public Message getMessage() {
            return this.message;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(this.message);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            this.message = (Message) in.readObject();
        }
    }
}
