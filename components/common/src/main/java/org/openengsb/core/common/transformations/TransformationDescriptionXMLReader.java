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

import org.openengsb.core.api.ekb.TransformationConstants;
import org.openengsb.core.api.ekb.transformation.TransformationDescription;
import org.openengsb.core.api.ekb.transformation.TransformationOperation;
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

    public TransformationDescriptionXMLReader() {
        descriptions = new ArrayList<TransformationDescription>();
        sourceFields = new ArrayList<String>();
        operationParams = new HashMap<String, String>();
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
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if (isIgnoreField(localName, false)) {
            return;
        }
        if (localName.equals("transformation")) {
            String source = attributes.getValue("source");
            String target = attributes.getValue("target");
            Class<?> sourceClass;
            Class<?> targetClass;
            try {
                sourceClass = getClass().getClassLoader().loadClass(source);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to load source class \"" + source + "\"", e);
            }
            try {
                targetClass = getClass().getClassLoader().loadClass(target);
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to load target class \"" + target + "\"", e);
            }
            activeDescription = new TransformationDescription(sourceClass, targetClass);
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
            switch (activeMode) {
                case FORWARD:
                    activeDescription.forwardField(sourceFields.get(0), targetField);
                    break;
                case CONCAT:
                    String concatString = operationParams.get(TransformationConstants.concatParam);
                    activeDescription.concatField(targetField, concatString,
                        sourceFields.toArray(new String[0]));
                    break;
                case SPLIT:
                    String splitString = operationParams.get(TransformationConstants.splitParam);
                    String index = operationParams.get(TransformationConstants.index);
                    activeDescription.splitField(sourceFields.get(0), targetField, splitString, index);
                    break;
                case MAP:
                    activeDescription.mapField(sourceFields.get(0), targetField, new HashMap<String, String>(
                        operationParams));
                    break;
                case SUBSTRING:
                    String from = operationParams.get(TransformationConstants.substringFrom);
                    String to = operationParams.get(TransformationConstants.substringTo);
                    activeDescription.substringField(sourceFields.get(0), targetField, from, to);
                    break;
                case VALUE:
                    String value = operationParams.get(TransformationConstants.value);
                    activeDescription.valueField(targetField, value);
                    break;
                default:
                    break;
            }
            activeMode = TransformationOperation.NONE;
            sourceFields.clear();
            operationParams.clear();
        }
    }

    public List<TransformationDescription> getResult() {
        return descriptions;
    }
}