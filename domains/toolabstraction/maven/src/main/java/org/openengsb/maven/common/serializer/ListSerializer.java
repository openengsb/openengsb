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

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ListSerializer<T> extends AbstractSerializer {

    public Element serializeList(List<T> exceptions, String listName, String listElement) throws DOMException,
            ParserConfigurationException {

        Element exceptionList = getDocument().createElement(listName);

        for (T e : exceptions) {
            Element exception = getDocument().createElement(listElement);
            exception.setTextContent(((Throwable) e).getMessage());
            exceptionList.appendChild(exception);
        }

        return exceptionList;
    }

    @SuppressWarnings("unchecked")
    public List<T> deserializeList(Node listElement, String listName) throws TransformerException {
        NodeList nodeList = listElement.getChildNodes();

        List<Exception> list = null;
        if (nodeList.getLength() > 0) {
            list = new ArrayList<Exception>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                list.add(new Exception(node.getTextContent()));
            }
        }
        return (List<T>) list;
    }
}
