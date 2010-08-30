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
package org.openengsb.core.config.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class AttributeDefinition implements Serializable {

    public static class Option {
        private final String label;
        private final String value;

        public Option(String label, String value) {
            super();
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
    }

    private String id;
    private String name = "";
    private String description = "";
    private String defaultValue = "";
    private boolean required;
    private final List<Option> options = new ArrayList<Option>();

    /**
     * Returns the attribute identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns a localizabled name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a localizabled description.
     */
    public String getDescription() {
        return description;
    }

    public boolean hasDescription() {
        return description != null && !description.isEmpty();
    }

    /**
     * Returns the default value.
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AttributeDefinition attribute;

        public Builder() {
            attribute = new AttributeDefinition();
        }

        public Builder id(String id) {
            attribute.id = id;
            return this;
        }

        public Builder name(String name) {
            attribute.name = name;
            return this;
        }

        public Builder description(String description) {
            attribute.description = description;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            attribute.defaultValue = defaultValue;
            return this;
        }

        public Builder required() {
            attribute.required = true;
            return this;
        }

        public Builder option(String label, String value) {
            attribute.options.add(new Option(label, value));
            return this;
        }

        public AttributeDefinition build() {
            return attribute;
        }
    }
}
