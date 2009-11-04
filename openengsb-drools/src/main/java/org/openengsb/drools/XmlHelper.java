package org.openengsb.drools;

import java.io.IOException;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openengsb.drools.model.Event;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XmlHelper {

	private static SourceTransformer t = new SourceTransformer();

	public static Event parseEvent(NormalizedMessage msg)
			throws TransformerException, ParserConfigurationException,
			IOException, SAXException, MessagingException {
		Element source = t.toDOMElement(msg);
		Node nameNode = source.getElementsByTagName("name").item(0);
		Event result = new Event(nameNode.getTextContent());
		Node contextNode = source.getElementsByTagName("contextid").item(0);
		if (contextNode != null) {
			result.setContextId(contextNode.getTextContent());
		}
		return result;
	}

}
