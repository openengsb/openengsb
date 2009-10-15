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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.util.IO;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Schema resolver who looks for schemas in the classpath.
 */
public class ClasspathEntityResolver implements EntityResolver {

    private Log log = LogFactory.getLog(getClass());

    private String pathValidationString;

    /**
     * The full html path required for each file which should be validated. This
     * could be for example "http://engsb.ifs.tuwien.ac.at/".
     */
    public void setPathValidationString(String pathValidationString) {
        this.pathValidationString = pathValidationString;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        this.log.trace("Validating schema with id [" + systemId + "] agains path [" + this.pathValidationString
                + "]...");
        if (!systemId.startsWith(this.pathValidationString)) {
            this.log.warn("Validation failed for [" + systemId + "]...");
            return null;
        }
        String schema = systemId.replaceAll(this.pathValidationString.replaceAll("\\.", "\\."), "META-INF");
        this.log.trace("Loading schema from classpath [" + schema + "]");
        InputStream in = IO.getResourceAsStream(schema);
        return new InputSource(in);
    }
}
