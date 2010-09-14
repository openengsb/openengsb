/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ekb.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.SAXReader;
import org.openengsb.ekb.resources.exceptions.ConversionException;
import org.openengsb.ekb.resources.exceptions.TMapException;
import org.xml.sax.SAXException;

/**
 * TODOs non-text segment conversion transformations between different domain
 * concepts external service calls (load from TMap, execute) --> how to wait for
 * message?
 * 
 * @author Thomas Moser
 * 
 */
public class TMap {

    private static Log logger = LogFactory.getLog(TMap.class);

    private final static String TMAPSCHEMA = "tmap.xsd";

    private String tmapID;

    private String tmapVersion;

    private String sender;

    private String receiver;

    private OpenEngSBMessage outputMessage;

    private OpenEngSBMessage inputMessage;

    // Map<InputFormat,Map<OutputFormat,Converter>>
    private Map<String, Map<String, Converter>> converters = new HashMap<String, Map<String, Converter>>();

    @SuppressWarnings("unchecked")
    public TMap(URL tmapLocation) throws TMapException {
        logger.debug("Loading T-Map from: " + tmapLocation);
        try {
            InputStream in = tmapLocation.openStream();
            SAXReader reader = new SAXReader();
            Document doc = reader.read(in);

            logger.debug("Validate the T-Map...");
            try {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                StreamSource ssource = new StreamSource(TMap.class.getResourceAsStream(TMAPSCHEMA));
                Schema schema = factory.newSchema(ssource);
                Validator validator = schema.newValidator();
                validator.validate(new DocumentSource((Node) doc));
            } catch (SAXException e) {
                logger.error("Error validating TMap: " + tmapLocation.toExternalForm(), e);
                throw new TMapException("Error validating TMap: " + tmapLocation.toExternalForm() + ". "
                        + e.getMessage());
            } catch (IOException e) {
                logger.error("Error reading Tmap: " + tmapLocation.toExternalForm(), e);
                throw new TMapException("Error reading Tmap: " + tmapLocation.toExternalForm() + ". " + e.getMessage());
            }

            logger.debug("Loading output message of the T-Map...");
            Element output = (Element) doc.selectSingleNode("//tmap/outputMessage");
            this.receiver = output.attributeValue("receiver");
            String receiverMessageType = output.attributeValue("messageType");
            List<OpenEngSBMessageSegment> outputSegments = new ArrayList<OpenEngSBMessageSegment>();
            List<Element> segments = doc.selectNodes("//tmap/outputMessage/segment");
            for (Element segmentElement : segments) {
                String segmentName = segmentElement.attributeValue("name");
                String domainConcept = segmentElement.attributeValue("domainConcept");
                String format = segmentElement.attributeValue("format");
                outputSegments.add(new OpenEngSBMessageSegment(segmentName, domainConcept, format, null));
            }
            this.outputMessage = new OpenEngSBMessage(receiverMessageType, outputSegments);

            logger.debug("Loading input message of the T-Map...");
            Element input = (Element) doc.selectSingleNode("//tmap/inputMessage");
            this.sender = input.attributeValue("sender");
            String senderMessageType = input.attributeValue("messageType");
            List<OpenEngSBMessageSegment> inputSegments = new ArrayList<OpenEngSBMessageSegment>();
            segments = doc.selectNodes("//tmap/inputMessage/segment");
            for (Element segmentElement : segments) {
                String segmentName = segmentElement.attributeValue("name");
                String domainConcept = segmentElement.attributeValue("domainConcept");
                String format = segmentElement.attributeValue("format");
                inputSegments.add(new OpenEngSBMessageSegment(segmentName, domainConcept, format, null));
            }
            this.inputMessage = new OpenEngSBMessage(senderMessageType, inputSegments);

            logger.debug("Loading converters of the T-Map...");
            List<Element> converters = doc.selectNodes("//tmap/transformation/converters/converter");
            for (Element converterElement : converters) {
                String inputFormat = converterElement.attributeValue("inputFormat");
                String outputFormat = converterElement.attributeValue("outputFormat");
                String className = converterElement.attributeValue("className");
                try {
                    Converter converter = (Converter) Class.forName(className).newInstance();
                    if (this.getConverters().get(inputFormat) == null)
                        this.getConverters().put(inputFormat, new HashMap<String, Converter>());
                    this.getConverters().get(inputFormat).put(outputFormat, converter);
                } catch (Exception ex) {
                    logger.error("Error loading converter: " + ex.getMessage(), ex);
                    throw new TMapException(ex.getMessage());
                }
            }

            logger.debug("Successfully loaded the T-Map from: " + tmapLocation);

        } catch (MalformedURLException e) {
            logger.error("Error loading T-Map! No valid url for T-Map: " + tmapLocation, e);
            throw new TMapException(e.getMessage());
        } catch (IOException e) {
            logger.error("Error loading T-Map! IOException occured", e);
            throw new TMapException(e.getMessage());
        } catch (DocumentException e) {
            logger.error("Error loading T-Map! T-Map is not a valid XML file: " + tmapLocation, e);
            throw new TMapException(e.getMessage());
        }
    }

    public OpenEngSBMessage performTransformation(OpenEngSBMessage inputMessage) throws TMapException {
        logger.debug("Starting the transformation for each output segment...");
        List<OpenEngSBMessageSegment> outputSegments = new ArrayList<OpenEngSBMessageSegment>();
        for (OpenEngSBMessageSegment inputSegment : inputMessage.getPayload()) {
            String inputDomainConcept = inputSegment.getDomainConcept();
            String inputFormat = inputSegment.getFormat();
            String inputSegmentContent = inputSegment.getContent();

            for (OpenEngSBMessageSegment outputSegment : this.getOutputMessage().getPayload()) {
                // if domainConcept and format of input and output segment
                // are the same
                if (inputDomainConcept.equals(outputSegment.getDomainConcept())
                        && inputFormat.equals(outputSegment.getFormat())) {
                    outputSegments.add(new OpenEngSBMessageSegment(outputSegment.getSegmentName(), outputSegment
                            .getDomainConcept(), outputSegment.getFormat(), inputSegmentContent));
                    break;
                }
                // if domainConcept is the same, but format is different a
                // converter needs to be called
                else if (inputDomainConcept.equals(outputSegment.getDomainConcept())) {
                    if (this.getConverters().get(inputFormat) == null
                            || this.getConverters().get(inputFormat).get(outputSegment.getFormat()) == null) {
                        logger.error("Error during Transformation: No suitable Converter available!");
                        throw new TMapException("Error during Transformation: No suitable Converter available!");
                    }
                    try {
                        outputSegments.add(new OpenEngSBMessageSegment(outputSegment.getSegmentName(), outputSegment
                                .getDomainConcept(), outputSegment.getFormat(), this.getConverters().get(inputFormat)
                                .get(outputSegment.getFormat()).convert(inputSegmentContent).toString()));
                    } catch (ConversionException ex) {
                        logger.error("Error during Transformation", ex);
                        throw new TMapException("Error during Transformation: " + ex.getMessage());
                    }
                }
            }
        }
        logger.debug("Transformation to output segments finished!");
        logger.debug("Create output message.");
        OpenEngSBMessage outputMessage = new OpenEngSBMessage(inputMessage.getMessageID(), inputMessage.getCreated(),
                inputMessage.getSender(), inputMessage.getReceiver(), inputMessage.getMessageType(), outputSegments);
        logger.debug("Transformation finished!");
        return outputMessage;
    }

    public OpenEngSBMessage getOutputMessage() {
        return this.outputMessage;
    }

    public OpenEngSBMessage getInputMessage() {
        return this.inputMessage;
    }

    public String getTmapID() {
        return this.tmapID;
    }

    public String getTmapVersion() {
        return this.tmapVersion;
    }

    public String getSender() {
        return this.sender;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public Map<String, Map<String, Converter>> getConverters() {
        return this.converters;
    }
}
