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

import java.util.ArrayList;
import java.util.List;

public class ServiceDescriptor {
    private String id;
    private String serviceInterface;
    private String name;
    private String description;
    private final List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();

    public ServiceDescriptor() {
    }

    /**
     * Returns an id that uniquely identifies this managed service.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the service interface id this service implements.
     */
    public String getServiceInterfaceId() {
        return serviceInterface;
    }

    /**
     * Returns a localized name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a localized description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a list of attributes the described service supports.
     */
    public List<AttributeDefinition> getAttributes() {
        return attributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ServiceDescriptor desc;

        public Builder() {
            desc = new ServiceDescriptor();
        }

        public Builder id(String id) {
            desc.id = id;
            return this;
        }

        public Builder implementsInterface(String serviceInterface) {
            desc.serviceInterface = serviceInterface;
            return this;
        }

        public Builder name(String name) {
            desc.name = name;
            return this;
        }

        public Builder description(String description) {
            desc.description = description;
            return this;
        }

        public Builder attribute(AttributeDefinition ad) {
            desc.attributes.add(ad);
            return this;
        }

        public ServiceDescriptor build() {
            return desc;
        }
    }
}
