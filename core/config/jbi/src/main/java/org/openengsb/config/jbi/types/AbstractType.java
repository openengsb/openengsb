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
package org.openengsb.config.jbi.types;

import java.io.Serializable;
import java.util.Map;

import org.w3c.dom.Element;

@SuppressWarnings("serial")
public abstract class AbstractType implements Serializable {
    private String name;
    private boolean optional;
    private int maxLength;
    private String defaultValue;

    public AbstractType() {
    }

    public AbstractType(String name, boolean optional, int maxLength, String defaultValue) {
        this.name = name;
        this.optional = optional;
        this.maxLength = maxLength;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Appends this type with value from the context as attribute to the
     * element.
     */
    public void toAttributeOnElement(Map<String, String> context, Element elem) {
        String value = context.get(name);
        if (value == defaultValue) {
            return;
        } else if (value == null) {
            value = defaultValue;
        }
        elem.setAttribute(name, value);
    }

    /**
     * Appends this type with value from the context as Spring property to the
     * element.
     */
    public void toPropertyOnElement(Map<String, String> context, Element elem) {
        String value = context.get(name);
        if (value == defaultValue) {
            return;
        } else if (value == null) {
            value = defaultValue;
        }
        Element p = elem.getOwnerDocument().createElement("property");
        elem.appendChild(p);
        p.setAttribute("name", name);
        p.setAttribute("value", value);
    }
}
