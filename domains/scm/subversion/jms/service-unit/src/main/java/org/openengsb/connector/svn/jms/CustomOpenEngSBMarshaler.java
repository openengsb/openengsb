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
package org.openengsb.connector.svn.jms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.servicemix.common.JbiConstants;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jms.endpoints.AbstractJmsMarshaler;
import org.apache.servicemix.jms.endpoints.JmsConsumerMarshaler;
import org.apache.servicemix.soap.core.MessageImpl;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.drools.events.ScmCheckInEvent;

public class CustomOpenEngSBMarshaler extends AbstractJmsMarshaler implements JmsConsumerMarshaler {

    private URI mep = JbiConstants.IN_ONLY;

    public JmsContext createContext(Message message) throws Exception {
        return new Context(message);
    }

    public MessageExchange createExchange(JmsContext jmsContext, ComponentContext jbiContext) throws Exception {
        MessageExchange exchange = jbiContext.getDeliveryChannel().createExchangeFactory().createExchange(this.mep);
        NormalizedMessage inMessage = exchange.createMessage();

        ScmCheckInEvent event = new ScmCheckInEvent();
        String xml = Transformer.toXml(event);
        inMessage.setContent(new StringSource(xml));

        String contextId = "42"; // TODO: find a reasonable way to get contextId
        MessageProperties msgProperties = new MessageProperties(contextId, UUID.randomUUID().toString());
        msgProperties.applyToMessage(inMessage);
        exchange.setOperation(new QName("event"));
        exchange.setMessage(inMessage, "in");
        return exchange;
    }

    public Message createOut(MessageExchange exchange, NormalizedMessage outMsg, Session session, JmsContext context)
            throws Exception {
        if (outMsg == null) {
            return session.createTextMessage("");
        }

        String xmlString = String.valueOf(new SourceTransformer().toString(outMsg.getContent()));
        TextMessage text = session.createTextMessage(xmlString);

        return text;
    }

    public Message createFault(MessageExchange exchange, Fault fault, Session session, JmsContext context)
            throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        org.apache.servicemix.soap.api.Message msg = new MessageImpl();
        msg.setContent(Source.class, fault.getContent());
        msg.setContent(OutputStream.class, baos);
        TextMessage text = session.createTextMessage(baos.toString());
        if (msg.get(org.apache.servicemix.soap.api.Message.CONTENT_TYPE) != null) {
            text.setStringProperty(CONTENT_TYPE_PROPERTY, (String) msg
                    .get(org.apache.servicemix.soap.api.Message.CONTENT_TYPE));
        }
        text.setBooleanProperty(FAULT_JMS_PROPERTY, true);
        return text;
    }

    public Message createError(MessageExchange exchange, Exception error, Session session, JmsContext context)
            throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(baos);
        error.printStackTrace(printWriter);
        printWriter.flush();
        printWriter.close();

        TextMessage message = session.createTextMessage(baos.toString());
        message.setBooleanProperty(ERROR_JMS_PROPERTY, true);
        return message;
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
