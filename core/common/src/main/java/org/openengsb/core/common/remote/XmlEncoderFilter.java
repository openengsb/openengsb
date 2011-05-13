package org.openengsb.core.common.remote;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

/**
 * This filter takes a {@link String} representing an XML-document and parses it into a {@link Document} The document is
 * passed to the next filter. The resulting document is then printed to a string again and returned. This filter can be
 * used regardless of what Object the XML-document may represent.
 *
 * This filter is intended for outgoing ports.
 *
 * <code>
 * <pre>
 *      [org.w3c.dom.Document] > Filter > [XML as String]   > ...
 *                                                             |
 *                                                             v
 *      [org.w3c.dom.Document] < Filter < [XML as String]   < ...
 * </pre>
 * </code>
 */
public class XmlEncoderFilter extends AbstractFilterChainElement<Document, Document> {

    private FilterAction next;

    public XmlEncoderFilter() {
        super(Document.class, Document.class);
    }

    @Override
    public Document doFilter(Document input, Map<String, Object> metadata) throws FilterException {
        String docString;
        try {
            docString = writeDocument(input);
        } catch (TransformerException e) {
            throw new FilterException(e);
        }
        String result = (String) next.filter(docString, metadata);
        return parseDocument(result);
    }

    @Override
    public void setNext(FilterAction next) {
        checkNextInputAndOutputTypes(next, Document.class, Document.class);
        Preconditions.checkArgument(next.getSupportedInputType().isAssignableFrom(Document.class));
        Preconditions.checkArgument(next.getSupportedOutputType().isAssignableFrom(Document.class));
        this.next = next;
    }

    public static Document parseDocument(String input) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(new StringReader(input)));
        } catch (ParserConfigurationException e) {
            throw new FilterException(e);
        } catch (SAXException e) {
            throw new FilterException(e);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        return doc;
    }

    public static String writeDocument(Node input) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter sw = new StringWriter();

        transformer.transform(new DOMSource(input), new StreamResult(sw));

        return sw.toString();
    }

}
