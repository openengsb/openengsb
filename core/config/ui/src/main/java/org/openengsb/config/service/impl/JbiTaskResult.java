package org.openengsb.config.service.impl;

import java.io.StringReader;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openengsb.config.jbi.MapNamespaceContext;
import org.xml.sax.InputSource;

public class JbiTaskResult {
	private InputSource xml;
	private String stringXml;
    private final XPath xpath;

    public JbiTaskResult(String xml) {
		this();
        this.xml = new InputSource(new StringReader(xml));
        this.stringXml = xml;
	}

	public JbiTaskResult() {
        this.xpath = XPathFactory.newInstance().newXPath();
        MapNamespaceContext context = new MapNamespaceContext();
        context.addNamespace("j", "http://java.sun.com/xml/ns/jbi/management-message");
        this.xpath.setNamespaceContext(context);
    }

	public void setAndCheck(String xml) throws Exception {
		this.stringXml = xml;
		this.xml = new InputSource(new StringReader(xml));
		if (!isSuccess()) {
			throw new Exception(stringXml);
		}
	}

    public boolean isSuccess() {
        String result = evaluateString("/j:jbi-task/j:jbi-task-result/j:frmwk-task-result/j:frmwk-task-result-details/j:task-result-details/j:task-result/text()");
        return result.trim().equalsIgnoreCase("SUCCESS");
    }

    private String evaluateString(String expression) {
        try {
            String result = (String) xpath.evaluate(expression, xml, XPathConstants.STRING);
            return result == null ? "" : result;
        } catch (XPathExpressionException e) {
            return "";
        }
    }

    @Override
    public String toString() {
        return stringXml;
    }
}
