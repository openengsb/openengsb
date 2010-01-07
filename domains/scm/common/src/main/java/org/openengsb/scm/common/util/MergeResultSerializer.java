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
import org.openengsb.scm.common.pojos.MergeResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * A Serializer for MergeResult, that can serialize a MergeResult-object to XML
 * and vice versa.
 */
public class MergeResultSerializer extends AbstractSerializer {
    private static final String MERGE_RESULT_ELEMENT_NAME = "message";

    private static final String REVISION_NAME = "revision";
    private static final String FILE_ELEMENT_NAME = "file";
    private static final String ADDS_NAME = "adds";
    private static final String DELETES_NAME = "deletes";
    private static final String MERGES_NAME = "merges";
    private static final String CONFLICTS_NAME = "conflicts";

    public static Source serialize(MergeResult result) throws SerialisationException {
        try {
            Element mergeResultElement = getDocument().createElement(MergeResultSerializer.MERGE_RESULT_ELEMENT_NAME);
            mergeResultElement.setAttribute(MergeResultSerializer.REVISION_NAME, result.getRevision());

            mergeResultElement.appendChild(createList(MergeResultSerializer.ADDS_NAME, result.getAdds()));
            mergeResultElement.appendChild(createList(MergeResultSerializer.DELETES_NAME, result.getDeletions()));
            mergeResultElement.appendChild(createList(MergeResultSerializer.MERGES_NAME, result.getMerges()));
            mergeResultElement.appendChild(createList(MergeResultSerializer.CONFLICTS_NAME, result.getConflicts()));

            return new DOMSource(mergeResultElement);
        } catch (DOMException exception) {
            throw new SerialisationException(exception);
        } catch (ParserConfigurationException exception) {
            throw new SerialisationException(exception);
        }
    }

    public static MergeResult deserialize(NormalizedMessage message) throws SerialisationException {
        try {
            MergeResult result = new MergeResult();

            // Grab the xml message
            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = (DOMSource) sourceTransformer.toDOMSource(message);
            Node rootNode = messageXml.getNode();

            String revision = getXpath().selectSingleNode(rootNode, "@" + MergeResultSerializer.REVISION_NAME)
                    .getNodeValue();
            result.setRevision(revision);

            // parse out all single lists
            result.setAdds(extractList(getXpath().selectSingleNode(rootNode, MergeResultSerializer.ADDS_NAME)));
            result.setDeletions(extractList(getXpath().selectSingleNode(rootNode, MergeResultSerializer.DELETES_NAME)));
            result.setMerges(extractList(getXpath().selectSingleNode(rootNode, MergeResultSerializer.MERGES_NAME)));
            result
                    .setConflicts(extractList(getXpath().selectSingleNode(rootNode,
                            MergeResultSerializer.CONFLICTS_NAME)));

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

    private static Element createList(String name, String[] files) throws DOMException, ParserConfigurationException {
        Element listElement = getDocument().createElement(name);

        if ((files != null) && (files.length > 0)) {
            // array is not empty to
            // preserve space
            for (String fileName : files) {
                Element fileElement = getDocument().createElement(MergeResultSerializer.FILE_ELEMENT_NAME);
                fileElement.setTextContent(fileName);

                listElement.appendChild(fileElement);
            }
        }

        return listElement;
    }

    private static String[] extractList(Node listElement) throws TransformerException {
        NodeList nodeList = getXpath().selectNodeList(listElement, MergeResultSerializer.FILE_ELEMENT_NAME);

        String[] list;
        if (nodeList.getLength() > 0) {
            list = new String[nodeList.getLength()];
            for (int i = 0; i < nodeList.getLength(); i++) {
                list[i] = nodeList.item(i).getTextContent();
            }
        } else {
            list = new String[0];
        }

        return list;
    }
}
