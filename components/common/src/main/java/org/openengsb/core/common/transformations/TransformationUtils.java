/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.transformations;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Provides some utility methods in the area of model transformations.
 */
public final class TransformationUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationUtils.class);

    private TransformationUtils() {
    }

    /**
     * Scans an input stream for transformation descriptions and returns all successfully read transformation
     * descriptions.
     */
    public static List<TransformationDescription> getDescriptionsFromXMLInputStream(InputStream fileContent) {
        List<TransformationDescription> desc = new ArrayList<TransformationDescription>();
        try {
            desc = loadDescrtipionsFromXMLInputSource(new InputSource(fileContent), null);
        } catch (Exception e) {
            LOGGER.error("Unable to read the descriptions from input stream. ", e);
        }
        return desc;
    }

    /**
     * Scans a XML file for transformation descriptions and returns all successfully read transformation descriptions.
     */
    public static List<TransformationDescription> getDescriptionsFromXMLFile(File file) {
        List<TransformationDescription> desc = new ArrayList<TransformationDescription>();
        try {
            return loadDescrtipionsFromXMLInputSource(new InputSource(file.getAbsolutePath()), file.getName());
        } catch (Exception e) {
            LOGGER.error("Unable to read the descriptions from file " + file.getAbsolutePath(), e);
        }
        return desc;
    }

    /**
     * Does the actual parsing.
     */
    private static List<TransformationDescription> loadDescrtipionsFromXMLInputSource(InputSource source,
            String fileName)
        throws Exception {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        TransformationDescriptionXMLReader reader = new TransformationDescriptionXMLReader(fileName);
        xr.setContentHandler(reader);
        xr.parse(source);
        return reader.getResult();
    }
}
