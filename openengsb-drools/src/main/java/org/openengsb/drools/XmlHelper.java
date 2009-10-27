package org.openengsb.drools;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openengsb.drools.model.Event;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XmlHelper {

	public Event parseEvent(Element source) throws TransformerException,
			ParserConfigurationException, IOException, SAXException {
		Node nameNode = source.getElementsByTagName("name").item(0);
		return new Event(nameNode.getTextContent());
	}
}
