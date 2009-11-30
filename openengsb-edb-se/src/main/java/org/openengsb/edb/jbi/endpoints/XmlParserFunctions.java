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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.apache.log4j.Logger;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.openengsb.edb.core.api.EDBException;
import org.openengsb.edb.core.entities.GenericContent;
import org.openengsb.edb.core.entities.OperationType;
import org.openengsb.util.Prelude;
import org.xml.sax.SAXException;

public class XmlParserFunctions {

    private static SourceTransformer sourceTransformer = new SourceTransformer();

    private static Logger logger = Logger.getLogger(XmlParserFunctions.class);

    /**
     * Data-container for generic-content and operation
     */
    public static class ContentWrapper {
        private GenericContent content;
        /** default operation type is UPDATE */
        private OperationType operation = OperationType.UPDATE;

        public GenericContent getContent() {
            return this.content;
        }

        public OperationType getOperation() {
            return this.operation;
        }

        public void setContent(final GenericContent content) {
            this.content = content;
        }

        public void setOperation(final OperationType operation) {
            this.operation = operation;
        }
    }

    /**
     * Data-container for request info.
     */
    public static class RequestWrapper {

        private String repoId;
        private String headId;
        private int depth;

        public String getRepoId() {
            return this.repoId;
        }

        public String getHeadId() {
            return this.headId;
        }

        public int getDepth() {
            return this.depth;
        }

        public void setRepoId(final String repoId) {
            this.repoId = repoId;
        }

        public void setHeadId(final String headId) {
            this.headId = headId;
        }

        public void setDepth(final int depth) {
            this.depth = depth;
        }

        @Override
        public String toString() {
            return "RequestWrapper [repoId: " + this.repoId + ", headId: " + this.headId + ", depth: " + this.depth
                    + "]";
        }

    }

    public static EDBOperationType getMessageType(NormalizedMessage msg) throws IOException, SAXException,
            TransformerException, DocumentException {
        Document doc = readMessage(msg);
        if (doc.getRootElement().getName().equals("acmQueryRequestMessage")) {
            return EDBOperationType.QUERY;
        } else if (doc.getRootElement().getName().equals("acmPersistMessage")) {
            return EDBOperationType.COMMIT;
        } else if (doc.getRootElement().getName().equals("acmResetRequestMessage")) {
            return EDBOperationType.RESET;
        } else if (doc.getRootElement().getName().equals("acmLinkRegisterMessage")) {
            return EDBOperationType.REGISTER_LINK;
        } else if (doc.getRootElement().getName().equals("acmLinkExecutedMessage")) {
            return EDBOperationType.EXECUTE_LINK;
        } else if (doc.getRootElement().getName().equals("acmLinkQueryRequestMessage")) {
            return EDBOperationType.REQUEST_LINK;
        } else {
            throw new RuntimeException("root element could not be sorted..." + doc.getRootElement().getName());
        }
    }

    public static List<ContentWrapper> parseCommitMessage(NormalizedMessage msg, String repoBase)
            throws MessagingException, TransformerException, ParserConfigurationException, IOException, SAXException,
            DocumentException {

        Document doc = readMessage(msg);
        List<ContentWrapper> result = new ArrayList<ContentWrapper>();
        Element root = doc.getRootElement();
        Element body = root.element("body");

        XmlParserFunctions.logger.info("start searching");
        @SuppressWarnings("unchecked")
        List<Element> objects = body.elements("acmMessageObjects");
        for (Element e : objects) {
            result.add(parseCommitMessageItem(e, repoBase));
        }
        XmlParserFunctions.logger.info("search finished");

        return result;
    }

    public static List<String> parseQueryMessage(NormalizedMessage msg) throws IOException, SAXException,
            TransformerException, DocumentException {

        Document doc = readMessage(msg);
        Element root = doc.getRootElement();
        Element body = root.element("body");
        @SuppressWarnings("unchecked")
        List<Element> elements = body.elements(EdbEndpoint.QUERY_ELEMENT_NAME);
        // star search

        List<String> results = new ArrayList<String>();
        for (final Element element : elements) {
            results.add(translateQuery(element.getTextTrim()));
        }

        return results;

    }

    public static RequestWrapper parseResetMessage(NormalizedMessage msg) throws IOException, SAXException,
            TransformerException, DocumentException {

        Document doc = readMessage(msg);

        RequestWrapper req = new RequestWrapper();

        // TODO exception handling required?
        XmlParserFunctions.logger.info(doc.asXML());
        Element body = doc.getRootElement().element("body");
        Element headId = body.element("headId");
        req.setHeadId(headId.getTextTrim());

        if (body.element("repoId") == null) {
            req.setRepoId("");
        } else {
            req.setRepoId(body.element("repoId").getTextTrim());
        }
        if (body.element("depth") == null) {
            req.setDepth(EdbEndpoint.DEFAULT_DEPTH);
        } else {
            req.setDepth(Integer.valueOf(body.element("depth").getTextTrim()));
        }
        return req;
    }

    public static String buildCommitBody(List<ContentWrapper> persistedSignals, String commitId) {
        int expectedChars = persistedSignals.size() * (300 + 20 * 200);
        StringBuilder body = new StringBuilder(expectedChars);

        if (persistedSignals.size() == 0) {
            body.append("<acmMessageObjects>");
            buildElement("user", EdbEndpoint.DEFAULT_USER, body);
            buildElement(GenericContent.UUID_NAME, UUID.randomUUID().toString(), body);
            buildElement(GenericContent.PATH_NAME, "", body);
            body.append("<acmMessageObject>");
            buildElement("key", "emptyKey", body);
            buildElement("value", "emptyValue", body);
            body.append("</acmMessageObject>");
            body.append("</acmMessageObjects>");
        } else {
            for (ContentWrapper wrapper : persistedSignals) {

                GenericContent signal = wrapper.getContent();

                body.append("<acmMessageObjects>");

                buildElement("user", EdbEndpoint.DEFAULT_USER, body);
                buildElement(GenericContent.UUID_NAME, signal.getUUID(), body);
                buildElement(GenericContent.PATH_NAME, signal.getPath(), body);

                for (Entry<Object, Object> entry : signal.getEntireContent()) {
                    body.append("<acmMessageObject>");
                    buildElement("key", entry.getKey().toString(), body);
                    buildElement("value", entry.getValue().toString(), body);
                    body.append("</acmMessageObject>");
                }

                body.append("<operation>").append(wrapper.getOperation()).append("</operation>");

                body.append("</acmMessageObjects>");
            }
        }

        buildElement("headId", commitId, body);

        return body.toString();
    }

    public static String buildCommitErrorBody(String msg, String trace) {
        StringBuilder body = new StringBuilder();

        // body.append("<acmMessageObjects>");
        // body.append(buildElement("user", EdbEndpoint.DEFAULT_USER, body));
        // body.append(buildElement(GenericContent.UUID_NAME, "uuid", body));
        // body.append(buildElement(GenericContent.PATH_NAME, "path", body));
        // body.append("<acmMessageObject>");
        // body.append(buildElement("key", "none", body));
        // body.append(buildElement("value", "none", body));
        // body.append("</acmMessageObject>");
        // body.append("</acmMessageObjects>");

        body.append("<acmErrorObject>");
        buildElement("message", msg, body);
        // body.append(buildElement("stacktrace", trace, body));
        body.append("</acmErrorObject>");

        return body.toString();
    }

    public static String buildResetBody(String headId) {
        StringBuilder sb = new StringBuilder();
        return buildElement("headId", headId, sb).toString();
    }

    public static String buildResetErrorBody(String msg, String trace) {
        StringBuilder body = new StringBuilder();

        body.append("<acmErrorObject>");
        buildElement("message", msg, body);
        buildElement("stacktrace", trace, body);
        body.append("</acmErrorObject>");

        return body.toString();
    }

    public static String buildQueryBody(List<GenericContent> foundSignals) throws EDBException {

        String name = UUID.randomUUID().toString();
        File store = new File(name);
        try {
            storeToTmp(store, foundSignals);
            foundSignals.clear();
            return loadFromTmpStore(store);
        } catch (IOException e) {
            try {
                store.delete();
            } catch (Exception e1) {
                XmlParserFunctions.logger.warn("tmp store file could not be deleted");
            }
            throw new EDBException(e);
        }
    }

    private static String buildElement(String name, String content) {
        StringBuilder block = new StringBuilder();
        block.append("<").append(name).append(">");
        block.append("<![CDATA[").append(content).append("]]>");
        block.append("</").append(name).append(">");

        return block.toString();
    }

    private static StringBuilder buildElement(String name, String content, StringBuilder builder) {

        builder.append("<").append(name).append(">");
        builder.append("<![CDATA[").append(content).append("]]>");
        builder.append("</").append(name).append(">");

        return builder;
    }

    private static void storeToTmp(File store, List<GenericContent> foundSignals) throws IOException {
        if (store.exists()) {
            store.delete();
        }
        store.createNewFile();

        FileWriter writer = new FileWriter(store);
        for (GenericContent signal : foundSignals) {
            writer.append("<acmMessageObjects>");

            // TODO extract from incoming msg;
            writer.append(buildElement("user", "dummyUser"));
            writer.append(buildElement(GenericContent.UUID_NAME, signal.getUUID()));
            writer.append(buildElement(GenericContent.PATH_NAME, signal.getPath()));

            for (Entry<Object, Object> entry : signal.getEntireContent()) {
                writer.append("<acmMessageObject>");
                writer.append(buildElement("key", entry.getKey().toString()));
                writer.append(buildElement("value", entry.getValue().toString()));
                writer.append("</acmMessageObject>");
            }

            writer.append("</acmMessageObjects>");
            writer.append("\n");
        }
        writer.flush();
        writer.close();
    }

    private static String loadFromTmpStore(File file) throws IOException {
        try {
            StringBuilder builder = new StringBuilder((int) file.length());
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
            try {
                file.delete();
            } catch (Exception e) {
                // TODO what ?
            }
            return builder.toString();

        } catch (NegativeArraySizeException e) {
            throw new IOException("tmp store is seriously too large (or does exist)");
        }
    }

    /**
     * Take the 'names' of the path 'values' and extract their occurrences from
     * the element (dom4j.Element)
     */
    private static String[] extractPathElems(String[] names, Element element) {
        // "//acmMessageObject/key[text()=\""+names[0]+"\"]/.."
        // this xpath should produce the desired elements
        String[] values = Arrays.copyOf(names, names.length);
        List<String> namesList = new ArrayList<String>(Arrays.asList(values));

        @SuppressWarnings("unchecked")
        List<Element> pairs = element.elements("acmMessageObject");
        for (Element pair : pairs) {
            final String singleName = pair.element("key").getText().trim();
            if (namesList.contains(singleName)) {
                values[namesList.indexOf(singleName)] = pair.element("value").getText().trim();
            }
        }
        return values;
    }

    private static ContentWrapper parseCommitMessageItem(Element msgElement, String repositoryBase) {
        ContentWrapper content = new ContentWrapper();

        String abstractPath = msgElement.element(GenericContent.PATH_NAME).getText().trim();
        String[] names = Prelude.dePathize(abstractPath);

        String uuid = msgElement.element(GenericContent.UUID_NAME).getText().trim();
        GenericContent parsedMsgElement;
        if (uuid == null) {
            parsedMsgElement = new GenericContent(repositoryBase, names, extractPathElems(names, msgElement));
        } else {
            UUID uuidCreated;
            try {
                uuidCreated = UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                uuidCreated = UUID.randomUUID();
            }
            parsedMsgElement = new GenericContent(repositoryBase, names, extractPathElems(names, msgElement),
                    uuidCreated);
        }

        parsedMsgElement.setPath(msgElement.element(GenericContent.PATH_NAME).getText().trim());

        Element elem = msgElement.element(EdbEndpoint.COMMIT_OPERATION_TAG_NAME);
        if (elem != null) {
            content.setOperation(OperationType.valueOf(elem.getTextTrim()));
        }

        @SuppressWarnings("unchecked")
        List<Element> pairs = msgElement.elements("acmMessageObject");
        for (final Element pair : pairs) {
            parsedMsgElement.setProperty(pair.element("key").getText().trim(), pair.element("value").getText().trim());
        }
        content.setContent(parsedMsgElement);

        return content;
    }

    private static Document readMessage(NormalizedMessage msg) throws IOException, SAXException, TransformerException,
            DocumentException {
        SAXSource saxSource = XmlParserFunctions.sourceTransformer.toSAXSource(msg.getContent());
        XmlParserFunctions.logger.info("converted to saxsource");
        SAXReader reader = new SAXReader(saxSource.getXMLReader());
        XmlParserFunctions.logger.info("saxreader initialized");
        reader.setValidation(false);
        Document doc = reader.read(saxSource.getInputSource());
        return doc;
    }

    /**
     * Hacky String replacements for jcr to lucene syntax (hotfix that lived too
     * long)
     */
    @Deprecated
    private static String translateQuery(String query) {
        String result = query;
        if (result.equals("/") || result.equals("")) {
            result = "*";
        } else {
            // all other searches
            String pathPart = result;
            String propertyPart = "";
            // search with attributes
            if (result.contains("[")) {

                pathPart = result.substring(0, result.indexOf("["));

                propertyPart = result.substring(result.indexOf("["), result.length());

                propertyPart = propertyPart.replace('[', ' ');
                propertyPart = propertyPart.replaceAll("=", ":*");
                propertyPart = propertyPart.replaceAll("\\]", "* AND ");
                propertyPart = propertyPart.substring(0, propertyPart.length() - 5);
            }
            // adding wildcard at end of path
            if (!pathPart.endsWith("*")) {
                pathPart += "*";
            }
            pathPart = "path" + ":" + pathPart;
            if (propertyPart != "") {
                result = pathPart + " AND " + propertyPart;
            }
        }
        return result;
    }

}
