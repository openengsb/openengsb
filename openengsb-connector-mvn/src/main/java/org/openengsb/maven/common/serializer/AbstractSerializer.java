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

package org.openengsb.maven.common.serializer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;

/**
 * A superclass for all Serializers, that holds some convenience-methods.
 * 
 */
public abstract class AbstractSerializer {

    protected static final CachedXPathAPI xpath = new CachedXPathAPI();
    protected static Document document = null;

    protected static CachedXPathAPI getXpath() {
        return AbstractSerializer.xpath;
    }

    protected static Document getDocument() throws ParserConfigurationException {
        if (AbstractSerializer.document == null) {
            // get an instance of factory
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // get an instance of builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create an instance of DOM
            AbstractSerializer.document = db.newDocument();
        }

        return AbstractSerializer.document;
    }

}
