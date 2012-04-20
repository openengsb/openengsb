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
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException,
        IllegalArgumentException {
        super.startElement(uri, localName, qName, attributes);
        if (isIgnoreField(localName, false)) {
            return;
        }
        if (localName.equals("transformation")) {
            Class<?> sourceClass = loadClass(attributes.getValue("source"), true);
            Class<?> targetClass = loadClass(attributes.getValue("target"), false);
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
            Map<String, String> copy = new HashMap<String, String>(operationParams);
            activeDescription.addStep(activeMode, sourceFields, targetField, copy);
            activeMode = TransformationOperation.NONE;
            sourceFields.clear();
            operationParams.clear();
        }
    }

    /**
     * Tries to load a class through the class loader of the bundle. Throws an IllegalArgumentException if the class
     * can't be loaded.
     */
    private Class<?> loadClass(String className, boolean sourceClass) throws IllegalArgumentException {
        if (className == null) {
            String message = "One description doesnt contain a %s. Description loading aborted";
            throw new IllegalArgumentException(String.format(message, sourceClass ? "source class" : "target class"));
        }
        try {
            return getClass().getClassLoader().loadClass(className);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to load class \"" + className + "\"", e);
        }
    }

    public List<TransformationDescription> getResult() {
        return descriptions;
    }
}
