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

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.common.Domain;
import org.openengsb.core.common.l10n.LocalizableString;
import org.openengsb.core.common.l10n.StringLocalizer;
import org.openengsb.core.common.validation.FormValidator;

import com.google.common.base.Preconditions;

public class ServiceDescriptor {
    private String id;
    private Class<? extends Domain> serviceType;
    private LocalizableString name;
    private LocalizableString description;
    private Class<? extends Domain> implementationType;
    private final List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();
    private FormValidator formValidator;

    public ServiceDescriptor() {
    }

    /**
     * Returns an id that uniquely identifies this managed service.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the service interface id this service implements.
     */
    public Class<? extends Domain> getServiceType() {
        return serviceType;
    }

    public void setServiceType(Class<? extends Domain> serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * Returns the Class that implements this service
     */
    public Class<? extends Domain> getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(Class<? extends Domain> implementationType) {
        this.implementationType = implementationType;
    }

    /**
     * Returns a localizable name.
     */
    public LocalizableString getName() {
        return name;
    }

    /**
     * Returns a localized description.
     */
    public LocalizableString getDescription() {
        return description;
    }

    /**
     * Returns a list of attributes the described service supports.
     */
    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public void addAttribute(AttributeDefinition attribute) {
        attributes.add(attribute);
    }

    public FormValidator getFormValidator() {
        return formValidator;
    }

    public void setFormValidator(FormValidator validator) {
        this.formValidator = validator;
    }

    public static Builder builder(StringLocalizer strings) {
        return new Builder(strings);
    }

    public static class Builder {
        private final ServiceDescriptor desc;
        private final StringLocalizer strings;

        public Builder(StringLocalizer strings) {
            this.strings = strings;
            desc = new ServiceDescriptor();
        }

        public Builder id(String id) {
            desc.id = id;
            return this;
        }

        public Builder serviceType(Class<? extends Domain> serviceType) {
            desc.serviceType = serviceType;
            return this;
        }

        public Builder implementationType(Class<? extends Domain> type) {
            desc.implementationType = type;
            return this;
        }

        public Builder name(String key, String... parameters) {
            desc.name = strings.getString(key, parameters);
            return this;
        }

        public Builder description(String key, String... parameters) {
            desc.description = strings.getString(key, parameters);
            return this;
        }

        public Builder attribute(AttributeDefinition ad) {
            desc.attributes.add(ad);
            return this;
        }

        public Builder formValidator(FormValidator validator) {
            desc.formValidator = validator;
            return this;
        }

        public ServiceDescriptor build() {
            Preconditions.checkState(desc.id != null && !desc.id.trim().isEmpty(), "id has not been set");
            Preconditions.checkState(desc.serviceType != null, "service type has not been set");
            Preconditions.checkState(desc.implementationType != null, "implementation type has not been set");
            Preconditions.checkState(desc.serviceType.isAssignableFrom(desc.implementationType),
                    "implementatio type is not compatible to service type");
            Preconditions.checkState(desc.name != null && !desc.name.getKey().trim().isEmpty(),
                    "service name has not been set");
            Preconditions.checkState(desc.description != null && !desc.description.getKey().trim().isEmpty(),
                    "service description has not been set");
            return desc;
        }

        public AttributeDefinition.Builder newAttribute() {
            return AttributeDefinition.builder(strings);
        }
    }
}
