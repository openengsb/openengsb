/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openengsb.core.common.l10n.LocalizableString;
import org.openengsb.core.common.l10n.PassThroughLocalizableString;
import org.openengsb.core.common.l10n.StringLocalizer;
import org.openengsb.core.common.validation.FieldValidator;
import org.openengsb.core.common.validation.SingleAttributeValidationResult;
import org.openengsb.core.common.validation.ValidationResultImpl;

import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public class AttributeDefinition implements Serializable {

    public static class Option implements Serializable {
        private final LocalizableString label;
        private final String value;

        public Option(LocalizableString label, String value) {
            super();
            this.label = label;
            this.value = value;
        }

        public LocalizableString getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }
    }

    private String id;
    private LocalizableString name;
    private LocalizableString description;
    private LocalizableString defaultValue = new PassThroughLocalizableString("");
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
     * Returns a localizable name.
     */
    public LocalizableString getName() {
        return name;
    }

    /**
     * Returns a localizable description.
     */
    public LocalizableString getDescription() {
        return description;
    }

    public boolean hasDescription() {
        return description != null && description.getKey() != null && !description.getKey().trim().equals("");
    }

    /**
     * Returns the default value.
     */
    public LocalizableString getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public boolean isPassword() {
        return isPassword;
    }

    public FieldValidator getValidator() {
        return this.validator;
    }

    public static Builder builder(StringLocalizer strings) {
        return new Builder(strings);
    }

    public static class Builder implements Serializable {
        private final AttributeDefinition attr;
        private final StringLocalizer strings;

        public Builder(StringLocalizer strings) {
            this.strings = strings;
            attr = new AttributeDefinition();
        }

        public Builder id(String id) {
            attr.id = id;
            return this;
        }

        public Builder name(String key) {
            attr.name = strings.getString(key);
            return this;
        }

        public Builder description(String key) {
            attr.description = strings.getString(key);
            return this;
        }

        public Builder defaultValue(String key) {
            attr.defaultValue = strings.getString(key);
            return this;
        }

        public Builder required() {
            attr.required = true;
            return this;
        }

        public Builder option(String labelKey, String value) {
            attr.options.add(new Option(strings.getString(labelKey), value));
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
            Preconditions.checkState(attr.name != null, "name not set");
            checkNotEmpty(attr.name.getKey(), "name is empty");
            for (Option o : attr.options) {
                Preconditions.checkState(o.getLabel() != null, "option label not set");
                checkNotEmpty(o.getLabel().getKey(), "option has empty label");
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

    private static final class AllValidValidator implements FieldValidator {

        @Override
        public SingleAttributeValidationResult validate(String validate) {
            return new ValidationResultImpl(true, "");
        }

    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name.getString(null), id);
    }

}
