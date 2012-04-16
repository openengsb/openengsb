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

package org.openengsb.core.ekb.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openengsb.core.api.ekb.transformation.TransformationDescription;
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
    private MODE activeMode;
    private List<String> sourceFields;
    private List<String> targetFields;
    private String operationParam;
    private boolean sourceField = false;
    private boolean targetField = false;
    private boolean paramField = false;

    private enum MODE {
        FORWARD, CONCAT, SPLIT, NONE
    }

    public TransformationDescriptionXMLReader() {
        descriptions = new ArrayList<TransformationDescription>();
        sourceFields = new ArrayList<String>();
        targetFields = new ArrayList<String>();
    }

    private boolean isIgnoreField(String fieldName, boolean isEndElement) {
        List<String> ignores = Arrays.asList("transformations", "target-fields", "source-fields");
        if (isEndElement) {
            ignores = new ArrayList<String>(ignores);
            ignores.add("target-field");
            ignores.add("source-field");
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
            sourceField = true;
        } else if (localName.equals("target-field")) {
            targetField = true;
        } else if (localName.equals("operation-string")) {
            paramField = true;
        } else {
            activeMode = Enum.valueOf(MODE.class, localName.toUpperCase());
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if (sourceField) {
            sourceFields.add(new String(ch).substring(start, start + length));
            sourceField = false;
        } else if (targetField) {
            targetFields.add(new String(ch).substring(start, start + length));
            targetField = false;
        } else if (paramField) {
            operationParam = new String(ch).substring(start, start + length);
            paramField = false;
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
                    activeDescription.forwardField(sourceFields.get(0), targetFields.get(0));
                    break;
                case CONCAT:
                    activeDescription.concatField(targetFields.get(0), operationParam,
                        sourceFields.toArray(new String[0]));
                    break;
                case SPLIT:
                    activeDescription.splitField(sourceFields.get(0), operationParam,
                        targetFields.toArray(new String[0]));
                    break;
                default:
                    break;
            }
            activeMode = MODE.NONE;
            sourceFields.clear();
            targetFields.clear();
            operationParam = "";
        }
    }

    public List<TransformationDescription> getResult() {
        return descriptions;
    }
}
