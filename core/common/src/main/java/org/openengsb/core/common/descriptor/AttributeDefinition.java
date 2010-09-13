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
package org.openengsb.core.common.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.openengsb.core.common.util.BundleStrings;
import org.openengsb.core.common.validation.FieldValidator;
import org.openengsb.core.common.validation.FieldValidationResult;
import org.openengsb.core.common.validation.ValidationResultImpl;

import com.google.common.base.Preconditions;

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
    private boolean isBoolean;
    private boolean isPassword;
    private FieldValidator validator = new AllValidValidator();

    /**
     * Returns the attribute identifier.
     */
    public String getId() {
        return id;
    }
    
    /**
     * @deprecated This method does not use validation to check your input. Only for testing purposes. Please use AttributeDefinition.builder instead.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns a localizabled name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @deprecated This method does not use validation to check your input. Only for testing purposes. Please use AttributeDefinition.builder instead.
     */
    public void setName(String name) {
        this.name = name;
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
     * @deprecated This method does not use validation to check your input. Only for testing purposes. Please use AttributeDefinition.builder instead.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the default value.
     */
    public String getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * @deprecated This method does not use validation to check your input. Only for testing purposes. Please use AttributeDefinition.builder instead.
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }
    
    /**
     * @deprecated This method does not use validation to check your input. Only for testing purposes. Please use AttributeDefinition.builder instead.
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }
    
    /**
     * @deprecated This method does not use validation to check your input. Only for testing purposes. Please use AttributeDefinition.builder instead.
     */
    public void addOption(String label, String value) {
        options.add(new Option(label, value));
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public boolean isPassword() {
        return isPassword;
    }
    
    /**
     * @deprecated This method does not use validation to check your input. Only for testing purposes. Please use AttributeDefinition.builder instead.
     */
    public void setPassword(boolean isPassword) {
        this.isPassword = isPassword;
    }

    /**
     * @deprecated This method does not use validation to check your input. Only for testing purposes. Please use AttributeDefinition.builder instead.
     */
    public void setBoolean(boolean isBoolean) {
        this.isBoolean = isBoolean;
    }

    public FieldValidator getValidator() {
        return this.validator;
    }

    /**
     * @deprecated This method does not use validation to check your input. Only for testing purposes. Please use AttributeDefinition.builder instead.
     */
    public void setValidator(FieldValidator validator) {
        this.validator = validator;
    }

    public static Builder builder(Locale locale, BundleStrings strings) {
        return new Builder(locale, strings);
    }

    public static class Builder {
        private final AttributeDefinition attr;
        private final BundleStrings strings;
        private final Locale locale;

        public Builder(Locale locale, BundleStrings strings) {
            this.locale = locale;
            this.strings = strings;
            attr = new AttributeDefinition();
        }

        public Builder id(String id) {
            attr.id = id;
            return this;
        }

        public Builder name(String key) {
            attr.name = strings.getString(key, locale);
            return this;
        }

        public Builder description(String key) {
            attr.description = strings.getString(key, locale);
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            attr.defaultValue = defaultValue;
            return this;
        }

        public Builder defaultValueLocalized(String key) {
            attr.defaultValue = strings.getString(key, locale);
            return this;
        }

        public Builder required() {
            attr.required = true;
            return this;
        }

        public Builder option(String labelKey, String value) {
            attr.options.add(new Option(strings.getString(labelKey, locale), value));
            return this;
        }

        public Builder asBoolean() {
            attr.isBoolean = true;
            return this;
        }

        public Builder asPassword() {
            attr.isPassword = true;
            return this;
        }

        public Builder validator(FieldValidator fieldValidator) {
            attr.validator = fieldValidator;
            return this;
        }

        private void checkNotEmpty(String value, String message) {
            Preconditions.checkState(value != null && !value.trim().isEmpty(), message + " for attribute " + attr.id);
        }

        public AttributeDefinition build() {
            Preconditions.checkState(attr.id != null && !attr.id.trim().isEmpty(), "attribute id not set");
            checkNotEmpty(attr.name, "name not set");
            for (Option o : attr.options) {
                checkNotEmpty(o.getLabel(), "option has empty label");
                checkNotEmpty(o.getValue(), "option has empty value");
            }
            Preconditions.checkState(!(attr.isBoolean && !attr.options.isEmpty()),
                    "boolean and options are incompatible");
            Preconditions.checkState(!(attr.isPassword && !attr.options.isEmpty()),
                    "password and options are incompatible");
            Preconditions.checkState(!(attr.isPassword && attr.isBoolean), "password and boolean are incompatible");
            return attr;
        }

    }
    
    private static final class AllValidValidator implements FieldValidator{

        @Override
        public FieldValidationResult validate(String validate) {
            return new ValidationResultImpl(true, "");
        }
        
    }

}
