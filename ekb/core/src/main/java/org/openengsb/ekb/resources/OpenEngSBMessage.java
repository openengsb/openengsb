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
package org.openengsb.ekb.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DocumentSource;
import org.openengsb.ekb.resources.exceptions.TMapException;
import org.openengsb.ekb.runtime.transformation.TMapProcessor;
import org.xml.sax.SAXException;

public class OpenEngSBMessage {

    private static Logger logger = Logger.getLogger(OpenEngSBMessage.class);

    private final static String ENGSBMESSAGESCHEMA = "engSBMessage.xsd";

    private String messageID;

    private String created;

    private String sender;

    private String receiver;

    private String messageType;

    private List<OpenEngSBMessageSegment> payload;

    public OpenEngSBMessage(String messageType, List<OpenEngSBMessageSegment> payload) {
        this.messageType = messageType;
        this.payload = payload;
    }

    public OpenEngSBMessage(String messageID, String created, String sender,
            String receiver, String messageType,
            List<OpenEngSBMessageSegment> payload) {
        this.messageID = messageID;
        this.created = created;
        this.sender = sender;
        this.receiver = receiver;
        this.messageType = messageType;
        this.payload = payload;
    }

    @SuppressWarnings("unchecked")
    public OpenEngSBMessage(Document xmlMessage) throws TMapException {
        logger.debug("Validate EngSB Message...");
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            StreamSource ssource = new StreamSource(TMapProcessor.class.getResourceAsStream(ENGSBMESSAGESCHEMA));
            Schema schema = factory.newSchema(ssource);
            Validator validator = schema.newValidator();
            validator.validate(new DocumentSource((Node) xmlMessage));
        } catch (SAXException e) {
            logger.error("Error validating EngSB Message: ", e);
            throw new TMapException("Error validating EngSB Message: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error reading EngSB Message: ", e);
            throw new TMapException("Error reading EngSB Message: " + e.getMessage());
        }
        logger.debug("EngSB Message validated!");
        logger.debug("Parsing Message Header.");
        this.messageID = (xmlMessage.selectSingleNode("//message/header/messageID")).getText();
        this.created = (xmlMessage.selectSingleNode("//message/header/created")).getText();
        this.sender = (xmlMessage.selectSingleNode("//message/header/sender")).getText();
        this.receiver = (xmlMessage.selectSingleNode("//message/header/receiver")).getText();
        this.messageType = (xmlMessage.selectSingleNode("//message/header/messageType")).getText();
        logger.debug("Parsing Message Payload.");
        this.payload = new ArrayList<OpenEngSBMessageSegment>();
        List<Element> segments = xmlMessage.selectNodes("//message/payload/segments/textSegment");
        for (Element segment : segments) {
            String segmentName = segment.attributeValue("name");
            String segmentDomainConcept = segment.attributeValue("domainConcept");
            String segmentFormat = segment.attributeValue("format");
            String segmentContent = segment.getText();
            this.payload.add(new OpenEngSBMessageSegment(segmentName, segmentDomainConcept, segmentFormat, segmentContent));
        }
        logger.debug("Succesfully parsed EngSB Message.");
    }

    public String getMessageID() {
        return this.messageID;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSender() {
        return this.sender;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public List<OpenEngSBMessageSegment> getPayload() {
        return this.payload;
    }

    public String getCreated() {
        return this.created;
    }

    public Document toXML() {
        Document document = DocumentHelper.createDocument();
        Element message = document.addElement("message");
        Element header = message.addElement("header");
        Element messageID = header.addElement("messageID");
        messageID.addText(this.getMessageID());
        Element created = header.addElement("created");
        created.addText(this.getCreated());
        Element sender = header.addElement("sender");
        sender.addText(this.getSender());
        Element receiver = header.addElement("receiver");
        receiver.addText(this.getReceiver());
        Element messageType = header.addElement("messageType");
        messageType.addText(this.getMessageType());
        Element payload = message.addElement("payload");
        Element segments = payload.addElement("segments");
        for (OpenEngSBMessageSegment segment : this.getPayload()) {
            Element textSegment = segments.addElement("textSegment");
            textSegment.addAttribute("name", segment.getSegmentName());
            textSegment.addAttribute("domainConcept", segment.getDomainConcept());
            textSegment.addAttribute("format", segment.getFormat());
            textSegment.addText(segment.getContent());
        }
        return document;
    }

}