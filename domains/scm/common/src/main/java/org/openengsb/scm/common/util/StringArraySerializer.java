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
package org.openengsb.scm.common.util;

import java.io.IOException;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openengsb.scm.common.exceptions.SerialisationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * A Serializer for MergeResult, that can serialize a generic String-array to
 * XML and vice versa.
 */
public class StringArraySerializer extends AbstractSerializer {
    private static final String ELEMENT_NAME = "element";

    public static Source serialize(String[] array, String name) throws SerialisationException {
        try {
            Element arrayXmlElement = getDocument().createElement(name);

            for (String string : array) {
                arrayXmlElement.appendChild(createElementNode(string));
            }

            return new DOMSource(arrayXmlElement);
        } catch (DOMException exception) {
            throw new SerialisationException(exception);
        } catch (ParserConfigurationException exception) {
            throw new SerialisationException(exception);
        }
    }

    private static Node createElementNode(String string) throws DOMException, ParserConfigurationException {
        Element element = getDocument().createElement(StringArraySerializer.ELEMENT_NAME);
        element.setTextContent(string);

        return element;
    }

    public static String[] deserialize(NormalizedMessage message) throws SerialisationException {
        try {
            // Grab the xml message
            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = (DOMSource) sourceTransformer.toDOMSource(message);
            Node rootNode = messageXml.getNode();

            // get all elements' value
            NodeList nodeList = getXpath().selectNodeList(rootNode, StringArraySerializer.ELEMENT_NAME);

            // set up array
            String[] result = new String[nodeList.getLength()];

            // fill array and return
            for (int i = 0; i < nodeList.getLength(); i++) {
                result[i] = nodeList.item(i).getTextContent();
            }

            return result;
        } catch (MessagingException exception) {
            throw new SerialisationException(exception);
        } catch (TransformerException exception) {
            throw new SerialisationException(exception);
        } catch (ParserConfigurationException exception) {
            throw new SerialisationException(exception);
        } catch (IOException exception) {
            throw new SerialisationException(exception);
        } catch (SAXException exception) {
            throw new SerialisationException(exception);
        }
    }

    public static String[] deserialize(Node node) throws SerialisationException {
        try {
            // get all elements' value
            NodeList nodeList = getXpath().selectNodeList(node, StringArraySerializer.ELEMENT_NAME);

            // set up array
            String[] result = new String[nodeList.getLength()];

            // fill array and return
            for (int i = 0; i < nodeList.getLength(); i++) {
                result[i] = nodeList.item(i).getTextContent();
            }

            return result;
        } catch (TransformerException exception) {
            throw new SerialisationException(exception);
        }
    }
}
