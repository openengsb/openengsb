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
package org.openengsb.core.config;

import java.util.Locale;
import java.util.Map;

import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.core.config.util.BundleStrings;

public interface ServiceInstanceFactory<DomainType extends Domain, InstanceType extends DomainType> {

    // TODO refactor locale, strings
    ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder, Locale locale, BundleStrings strings);

    void updateServiceInstance(InstanceType instance, Map<String, String> attributes);

    InstanceType createServiceInstance(String id, Map<String, String> attributes);
}