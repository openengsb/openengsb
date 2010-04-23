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

    public void setAndCheckResult(String xml) throws Exception {
		this.stringXml = xml;
		this.xml = new InputSource(new StringReader(xml));
        if (!wasSuccessful()) {
			throw new Exception(stringXml);
		}
	}

    public boolean wasSuccessful() {
        String result = evaluateXPathOnResult("/j:jbi-task/j:jbi-task-result/j:frmwk-task-result/j:frmwk-task-result-details/j:task-result-details/j:task-result/text()");
        return result.trim().equalsIgnoreCase("SUCCESS");
    }

    private String evaluateXPathOnResult(String expression) {
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
