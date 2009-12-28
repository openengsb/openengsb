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
package org.openengsb.util.schema;

import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Checks well-formedness and validates xml against the schemas specified in the
 * xml.
 */
public class XmlValidator {
    private final Log log = LogFactory.getLog(getClass());

    // xml parser feature names for XSD validation
    private static final String APACHE_XML_FEATURES_VALIDATION_SCHEMA = "http://apache.org/xml/features/validation/schema";
    private static final String APACHE_XML_FEATURES_VALIDATION_SCHEMA_FULL_CHECKING = "http://apache.org/xml/features/validation/schema-full-checking";
    // JAXP properties for specifying external XSD language
    private static final String JAXP_PROPERTIES_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String JAXP_PROPERTIES_SCHEMA_LANGUAGE_VALUE = "http://www.w3.org/2001/XMLSchema";

    private EntityResolver entityResolver;
    private final ThrowingErrorHandler throwingErrorHandler = new ThrowingErrorHandler();

    public void setEntityResolver(EntityResolver entityResolver) {
        this.entityResolver = entityResolver;
    }

    public boolean isWellFormed(String xml) {
        try {
            SAXReader reader = new SAXReader();
            reader.setEntityResolver(this.entityResolver);
            reader.setValidation(false);
            reader.read(new StringReader(xml));
            return true;
        } catch (Throwable e) {
            this.log.warn("well-formed check failed for message", e);
            return false;
        }
    }

    public boolean validate(String xml) {
        try {
            SAXReader reader = new SAXReader();
            reader.setEntityResolver(this.entityResolver);
            reader.setValidation(true);
            reader.setFeature(XmlValidator.APACHE_XML_FEATURES_VALIDATION_SCHEMA, true);
            reader.setFeature(XmlValidator.APACHE_XML_FEATURES_VALIDATION_SCHEMA_FULL_CHECKING, true);
            reader.setProperty(XmlValidator.JAXP_PROPERTIES_SCHEMA_LANGUAGE,
                    XmlValidator.JAXP_PROPERTIES_SCHEMA_LANGUAGE_VALUE);
            reader.setErrorHandler(this.throwingErrorHandler);
            reader.read(new StringReader(xml));
            return true;
        } catch (Throwable e) {
            this.log.warn("validation for message failed", e);
            return false;
        }
    }

    private static class ThrowingErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }

        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }

}
