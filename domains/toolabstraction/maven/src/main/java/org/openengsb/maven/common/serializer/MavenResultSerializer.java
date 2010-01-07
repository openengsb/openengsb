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

package org.openengsb.maven.common.serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openengsb.maven.common.exceptions.SerializationException;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The MavenResultSerializer creates a dom source of the <tt>MavenResult</tt>
 * object and reverse. </br> There are constants used to find the elements in
 * the dom source
 * 
 */
public class MavenResultSerializer extends AbstractSerializer {

    private static final String RESULTLIST_ELEMENT_NAME = "resultList";

    private static final String FILE = "file";
    private static final String DEPLOYED_FILE = "deployedFile";
    private static final String RESULT = "result";

    private static final String TIMESTAMP = "timestamp";
    private static final String TASK = "task";
    private static final String OUTPUT = "output";
    private static final String EXCEPTION_LIST = "exceptionList";
    private static final String EXCEPTION = "exception";
    private static final String ERRORMESSAGE = "errormessage";

    /**
     * Creates a dom source from the <tt>MavenResult</tt>
     * 
     * @param source - source of the out message
     * @param result - maven result
     * @return dom source - the generated dom source
     */
    public static Source serializeAsSource(Source source, MavenResult result) throws SerializationException {

        try {
            return new DOMSource(serializeAsElement(result));
        } catch (DOMException e) {
            throw new SerializationException(e);
        }
    }

    public static Source serialize(Source source, List<MavenResult> resultList) throws SerializationException {
        try {
            // set up containing element
            Element resultListElement = getDocument().createElement(MavenResultSerializer.RESULTLIST_ELEMENT_NAME);

            for (MavenResult result : resultList) {
                resultListElement.appendChild(serializeAsElement(result));
            }

            return new DOMSource(resultListElement);
        } catch (DOMException exception) {
            throw new SerializationException(exception);
        } catch (ParserConfigurationException exception) {
            throw new SerializationException(exception);
        }
    }

    /**
     * Creates a <tt>MavenResult-Object</tt> from the message
     * 
     * @param message - a <tt>NormalizedMessage</tt>, that would be transformed
     *        into a dom source
     * @return mavenresult - result of maven
     */
    public static MavenResult deserialize(NormalizedMessage message) throws SerializationException {
        MavenResult mavenResult = null;

        Node resultNode;

        try {
            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = (DOMSource) sourceTransformer.toDOMSource(message);

            // xpath funktioniert hier irgendwie nicht, scheinbar nur bei
            // untergeordnete
            resultNode = messageXml.getNode();

            if (resultNode != null) {

                mavenResult = new MavenResult();

                mavenResult.setTimestamp(Long.valueOf(AbstractSerializer.xpath.selectSingleNode(resultNode,
                        "@" + MavenResultSerializer.TIMESTAMP).getNodeValue()));
                mavenResult.setMavenOutput(AbstractSerializer.xpath.selectSingleNode(resultNode,
                        "@" + MavenResultSerializer.OUTPUT).getNodeValue());

                if (!mavenResult.getMavenOutput().equals(MavenResult.SUCCESS)) {
                    Node exceptionsNode = AbstractSerializer.xpath.selectSingleNode(resultNode,
                            MavenResultSerializer.EXCEPTION_LIST);

                    ListSerializer<Exception> listSerializer = new ListSerializer<Exception>();
                    List<Exception> exceptions = listSerializer.deserializeList(exceptionsNode,
                            MavenResultSerializer.EXCEPTION_LIST);

                    mavenResult.setExceptions(exceptions);
                    mavenResult.setTask(AbstractSerializer.xpath.selectSingleNode(resultNode,
                            "@" + MavenResultSerializer.TASK).getNodeValue());

                    mavenResult.setErrorMessage(AbstractSerializer.xpath.selectSingleNode(resultNode,
                            "@" + MavenResultSerializer.ERRORMESSAGE).getNodeValue());
                }
            }

        } catch (TransformerException e) {
            throw new SerializationException(e);
        } catch (MessagingException e) {
            throw new SerializationException(e);
        } catch (ParserConfigurationException e) {
            throw new SerializationException(e);
        } catch (IOException e) {
            throw new SerializationException(e);
        } catch (SAXException e) {
            throw new SerializationException(e);
        }

        return mavenResult;
    }

    public static List<MavenResult> deserializeList(NormalizedMessage message) throws SerializationException {
        // set up result-list
        List<MavenResult> resultList = new ArrayList<MavenResult>();

        try {
            // Grab the xml message
            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = (DOMSource) sourceTransformer.toDOMSource(message);

            // parse out all single results
            NodeList resultNodeList = AbstractSerializer.xpath.selectNodeList(messageXml.getNode(),
                    MavenResultSerializer.RESULT);

            // iterate over result nodes
            for (int i = 0; i < resultNodeList.getLength(); i++) {
                Node resultNode = resultNodeList.item(i);
                resultList.add(deserializeResult(resultNode));
            }
        } catch (MessagingException exception) {
            throw new SerializationException(exception);
        } catch (DOMException exception) {
            throw new SerializationException(exception);
        } catch (TransformerException exception) {
            throw new SerializationException(exception);
        } catch (ParserConfigurationException exception) {
            throw new SerializationException(exception);
        } catch (IOException exception) {
            throw new SerializationException(exception);
        } catch (SAXException exception) {
            throw new SerializationException(exception);
        }

        return resultList;
    }

    /**
     * Method only for testing the creation of the MavenResult from a source
     * 
     * @param source - source that includes the buildresult pattern
     * @return mavenresult - the created result
     */
    public static MavenResult deserializeSource(Source source) throws SerializationException {
        MavenResult mavenResult = null;

        Node resultNode;

        try {
            mavenResult = new MavenResult();

            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = sourceTransformer.toDOMSource(source);

            resultNode = AbstractSerializer.xpath.selectSingleNode(messageXml.getNode(), MavenResultSerializer.RESULT);

            resultNode = messageXml.getNode();

            mavenResult.setTimestamp(Long.valueOf(AbstractSerializer.xpath.selectSingleNode(resultNode,
                    "@" + MavenResultSerializer.TIMESTAMP).getNodeValue()));
            mavenResult.setMavenOutput(AbstractSerializer.xpath.selectSingleNode(resultNode,
                    "@" + MavenResultSerializer.OUTPUT).getNodeValue());

            if (!mavenResult.getMavenOutput().equals(MavenResult.SUCCESS)) {
                Node exceptionsNode = AbstractSerializer.xpath.selectSingleNode(resultNode,
                        MavenResultSerializer.EXCEPTION_LIST);

                NodeList list = exceptionsNode.getChildNodes();

                List<Exception> exceptions = new ArrayList<Exception>();
                for (int i = 0; i < list.getLength(); i++) {
                    Node node = list.item(i);
                    exceptions.add(new Exception(node.getTextContent()));
                }

                mavenResult.setExceptions(exceptions);
                mavenResult.setTask(AbstractSerializer.xpath.selectSingleNode(resultNode,
                        "@" + MavenResultSerializer.TASK).getNodeValue());

                mavenResult.setErrorMessage(AbstractSerializer.xpath.selectSingleNode(resultNode,
                        "@" + MavenResultSerializer.ERRORMESSAGE).getNodeValue());
            }

        } catch (TransformerException e) {
            throw new SerializationException(e);
        } catch (ParserConfigurationException e) {
            throw new SerializationException(e);
        } catch (IOException e) {
            throw new SerializationException(e);
        } catch (SAXException e) {
            throw new SerializationException(e);
        }

        return mavenResult;
    }

    public static List<MavenResult> deserializeListSource(Source source) throws SerializationException {
        // set up result-list
        List<MavenResult> resultList = new ArrayList<MavenResult>();

        try {
            // Grab the xml message
            SourceTransformer sourceTransformer = new SourceTransformer();
            DOMSource messageXml = sourceTransformer.toDOMSource(source);

            // parse out all single results
            NodeList resultNodeList = AbstractSerializer.xpath.selectNodeList(messageXml.getNode(),
                    MavenResultSerializer.RESULT);

            // iterate over result nodes
            for (int i = 0; i < resultNodeList.getLength(); i++) {
                Node resultNode = resultNodeList.item(i);
                resultList.add(deserializeResult(resultNode));
            }
        } catch (DOMException exception) {
            throw new SerializationException(exception);
        } catch (TransformerException exception) {
            throw new SerializationException(exception);
        } catch (ParserConfigurationException exception) {
            throw new SerializationException(exception);
        } catch (IOException exception) {
            throw new SerializationException(exception);
        } catch (SAXException exception) {
            throw new SerializationException(exception);
        }

        return resultList;
    }

    /* helpers */

    public static Element serializeAsElement(MavenResult result) throws SerializationException {

        Element resultElement = null;
        try {
            resultElement = getDocument().createElement(MavenResultSerializer.RESULT);
            resultElement.setAttribute(MavenResultSerializer.TIMESTAMP, String.valueOf(result.getTimestamp()));
            resultElement.setAttribute(MavenResultSerializer.OUTPUT, result.getMavenOutput());

            if (result.getFile() != null) {
                resultElement.setAttribute(MavenResultSerializer.FILE, result.getFile());
            }
            if (result.getDeployedFiles() != null) {
                StringArraySerializer arraySerializer = new StringArraySerializer();
                Element deployedFiles = arraySerializer.serialize(result.getDeployedFiles(),
                        MavenResultSerializer.DEPLOYED_FILE);
                resultElement.appendChild(deployedFiles);
            }

            if (!result.getMavenOutput().equals(MavenResult.SUCCESS)) {
                if (result.getExceptions() != null) {
                    ListSerializer<Exception> serializer = new ListSerializer<Exception>();
                    Element exceptionList = serializer.serializeList(result.getExceptions(),
                            MavenResultSerializer.EXCEPTION_LIST, MavenResultSerializer.EXCEPTION);
                    resultElement.appendChild(exceptionList);
                }

                resultElement.setAttribute(MavenResultSerializer.TASK, result.getTask());
                resultElement.setAttribute(MavenResultSerializer.ERRORMESSAGE, result.getErrorMessage());
            }

            return resultElement;
        } catch (DOMException exception) {
            throw new SerializationException(exception);
        } catch (ParserConfigurationException exception) {
            throw new SerializationException(exception);
        }
    }

    private static MavenResult deserializeResult(Node resultNode) throws DOMException, TransformerException,
            SerializationException {

        MavenResult mavenResult = null;

        if (resultNode != null) {

            mavenResult = new MavenResult();

            mavenResult.setTimestamp(Long.valueOf(AbstractSerializer.xpath.selectSingleNode(resultNode,
                    "@" + MavenResultSerializer.TIMESTAMP).getNodeValue()));
            mavenResult.setMavenOutput(AbstractSerializer.xpath.selectSingleNode(resultNode,
                    "@" + MavenResultSerializer.OUTPUT).getNodeValue());

            if (!mavenResult.getMavenOutput().equals(MavenResult.SUCCESS)) {
                Node exceptionsNode = AbstractSerializer.xpath.selectSingleNode(resultNode,
                        MavenResultSerializer.EXCEPTION_LIST);

                if (exceptionsNode != null) {
                    ListSerializer<Exception> listSerializer = new ListSerializer<Exception>();
                    List<Exception> exceptions = listSerializer.deserializeList(exceptionsNode,
                            MavenResultSerializer.EXCEPTION_LIST);

                    mavenResult.setExceptions(exceptions);
                }

                mavenResult.setTask(AbstractSerializer.xpath.selectSingleNode(resultNode,
                        "@" + MavenResultSerializer.TASK).getNodeValue());

                mavenResult.setErrorMessage(AbstractSerializer.xpath.selectSingleNode(resultNode,
                        "@" + MavenResultSerializer.ERRORMESSAGE).getNodeValue());
            }

            if (AbstractSerializer.xpath.selectSingleNode(resultNode, "@" + MavenResultSerializer.FILE) != null) {
                mavenResult.setFile(AbstractSerializer.xpath.selectSingleNode(resultNode,
                        "@" + MavenResultSerializer.FILE).getNodeValue());
            }

            Node deployedFilesNode = AbstractSerializer.xpath.selectSingleNode(resultNode,
                    MavenResultSerializer.DEPLOYED_FILE);
            if (deployedFilesNode != null) {
                StringArraySerializer arraySerializer = new StringArraySerializer();
                String[] deployedFiles = arraySerializer.deserialize(deployedFilesNode);
                mavenResult.setDeployedFiles(deployedFiles);
            }

        }

        return mavenResult;
    }
}