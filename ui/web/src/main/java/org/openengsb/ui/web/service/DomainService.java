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

package org.openengsb.ui.web.service;

import java.util.List;

import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.osgi.framework.ServiceReference;

public interface DomainService {
    List<DomainProvider> domains();

    List<ServiceManager> serviceManagersForDomain(Class<? extends Domain> domain);

    List<ServiceReference> serviceReferencesForConnector(Class<? extends Domain> connectorClass);

    List<? extends ServiceReference> getManagedServiceInstances();

    Object getService(ServiceReference serviceReference);

    Object getService(String serviceClass, String serviceId);
}
