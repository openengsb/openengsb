/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.drools.model;

import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.apache.servicemix.expression.JAXPBooleanXPathExpression;
import org.apache.servicemix.expression.JAXPStringXPathExpression;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;

public class Message {

    private static final SourceTransformer TRANFORMER = new SourceTransformer();
    
    private final NormalizedMessage message;
    private final NamespaceContext namespaceContext;
    
    public Message(NormalizedMessage message, NamespaceContext namespaceContext) {
        this.message = message;
        // Make sure message is re-readable
        this.namespaceContext = namespaceContext;
        Source content = message.getContent();
        if (content != null) {
            try {
                content = new DOMSource(TRANFORMER.toDOMElement(content));
                message.setContent(content);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
    }
    
    public NormalizedMessage getInternalMessage() {
        return this.message;
    }
    
    public boolean xpath(String xpath) throws Exception {
        JAXPBooleanXPathExpression expression = new JAXPBooleanXPathExpression(xpath);
        if (this.namespaceContext != null) {
            expression.setNamespaceContext(this.namespaceContext);
        }
        Boolean b = (Boolean) expression.evaluate(null, message);
        return b.booleanValue();
    }
    
    public String valueOf(String xpath) throws Exception {
        JAXPStringXPathExpression expression = new JAXPStringXPathExpression(xpath);
        if (this.namespaceContext != null) {
            expression.setNamespaceContext(this.namespaceContext);
        }
        return (String)expression.evaluate(null, message);
    }
    
    
    public Object getProperty(String name) {
        return message.getProperty(name);
    }
    
    public void setProperty(String name, Object value) {
        message.setProperty(name, value);
    }
    
    public Element getContent() {
        return (Element) ((DOMSource) message.getContent()).getNode();
    }
        
    public Object getXPath(String path) throws XPathExpressionException {
        Element msgXML = getContent();
        if(msgXML!=null) {
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node value = (Node) xpath.evaluate(path, getContent(), XPathConstants.NODE);
            if(value == null || value.getNodeType() != Node.ATTRIBUTE_NODE) {
                throw new DOMException(DOMException.NOT_FOUND_ERR, "Attribute not found in message with XPath: " + path);
            }
            return value.getNodeValue();
        } else {
            return null;
        }
    }
    
    public void setXPath(String path, Object value) throws XPathExpressionException {
        Element msgXML = getContent();
        if(msgXML!=null) {
            XPath xpath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xpath.evaluate(path, getContent(), XPathConstants.NODE);
            if (node == null || node.getNodeType() != Node.ATTRIBUTE_NODE) {
                throw new DOMException(DOMException.NOT_FOUND_ERR, "Attribute not found in message with xpath: "+ path);
            } else {
                node.setNodeValue(value.toString());
            }
        }        
    }
    
}
