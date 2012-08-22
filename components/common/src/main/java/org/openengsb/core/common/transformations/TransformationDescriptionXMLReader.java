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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * The TransformationDescriptionXMLReader parses a file for transformation descriptions. The transformation descriptions
 * are XML based. Their structure can be read in the manual.
 */
public class TransformationDescriptionXMLReader extends DefaultHandler2 {
    private List<TransformationDescription> descriptions;
    private TransformationDescription activeDescription;
    private TransformationOperation activeMode;
    private List<String> sourceFields;
    private String targetField;
    private Map<String, String> operationParams;
    private boolean activeSourceField = false;
    private boolean activeTargetField = false;
    private String fileName;

    public TransformationDescriptionXMLReader(String fileName) {
        descriptions = new ArrayList<TransformationDescription>();
        sourceFields = new ArrayList<String>();
        operationParams = new HashMap<String, String>();
        this.fileName = fileName;
    }

    private boolean isIgnoreField(String fieldName, boolean isEndElement) {
        List<String> ignores = Arrays.asList("transformations", "source-fields", "params");
        if (isEndElement) {
            ignores = new ArrayList<String>(ignores);
            ignores.add("target-field");
            ignores.add("source-field");
            ignores.add("param");
        }
        return ignores.contains(fieldName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException,
        IllegalArgumentException {
        super.startElement(uri, localName, qName, attributes);
        if (isIgnoreField(localName, false)) {
            return;
        }
        if (localName.equals("transformation")) {
            String source = attributes.getValue("source");
            String target = attributes.getValue("target");
            String id = attributes.getValue("id");
            
            String[] split = source.split(";");
            String className = split[0];
            String version = split.length > 1 ? split[1] : "1.0.0";
            ModelDescription sourceModel = new ModelDescription(className, version);
            
            split = target.split(";");
            className = split[0];
            version = split.length > 1 ? split[1] : "1.0.0";
            ModelDescription targetModel = new ModelDescription(className, version);
            
            activeDescription = new TransformationDescription(sourceModel, targetModel, id);
            activeDescription.setFileName(fileName);
        } else if (localName.equals("source-field")) {
            activeSourceField = true;
        } else if (localName.equals("target-field")) {
            activeTargetField = true;
        } else if (localName.equals("param")) {
            String key = attributes.getValue("key");
            String value = attributes.getValue("value");
            operationParams.put(key, value);
        } else {
            activeMode = Enum.valueOf(TransformationOperation.class, localName.toUpperCase());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (activeSourceField) {
            sourceFields.add(new String(ch).substring(start, start + length));
            activeSourceField = false;
        } else if (activeTargetField) {
            targetField = new String(ch).substring(start, start + length);
            activeTargetField = false;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (isIgnoreField(localName, true)) {
            return;
        }
        if (localName.equals("transformation")) {
            descriptions.add(activeDescription);
        } else {
            Map<String, String> copy = new HashMap<String, String>(operationParams);
            activeDescription.addStep(activeMode, sourceFields, targetField, copy);
            activeMode = TransformationOperation.NONE;
            sourceFields.clear();
            operationParams.clear();
        }
    }

    public List<TransformationDescription> getResult() {
        return descriptions;
    }
}
