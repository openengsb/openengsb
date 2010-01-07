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
import java.util.HashMap;
import java.util.Map;

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
 * A Serializer for MergeResult, that can serialize a String-Map, that is an
 * instance of Map<String, String> to XML and vice versa.
 */
public class StringMapSerializer extends AbstractSerializer {
    private static final String ENTRY_NAME = "entry";
    private static final String KEY_NAME = "key";
    private static final String VALUE_NAME = "value";

    public static Source serialize(Map<String, String> map, String name) throws SerialisationException {
        try {
            Element arrayXmlElement = getDocument().createElement(name);

            for (String key : map.keySet()) {
                Element entryElement = getDocument().createElement(StringMapSerializer.ENTRY_NAME);
                entryElement.setAttribute(StringMapSerializer.KEY_NAME, key);
                entryElement.setAttribute(StringMapSerializer.VALUE_NAME, map.get(key));

                arrayXmlElement.appendChild(entryElement);
            }

            return new DOMSource(arrayXmlElement);
        } catch (DOMException exception) {
            throw new SerialisationException(exception);
        } catch (ParserConfigurationException exception) {
            throw new SerialisationException(exception);
        }
    }

    public static Map<String, String> deserialize(NormalizedMessage message) throws SerialisationException {
        try {
            // Grab the xml message
            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = (DOMSource) sourceTransformer.toDOMSource(message);
            Node rootNode = messageXml.getNode();

            // get all entries
            NodeList nodeList = getXpath().selectNodeList(rootNode, StringMapSerializer.ENTRY_NAME);

            // set up map
            Map<String, String> result = new HashMap<String, String>(nodeList.getLength());

            // fill map and return
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String key = getXpath().selectSingleNode(node, "@" + StringMapSerializer.KEY_NAME).getNodeValue();
                String value = getXpath().selectSingleNode(node, "@" + StringMapSerializer.VALUE_NAME).getNodeValue();

                result.put(key, value);
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
}
