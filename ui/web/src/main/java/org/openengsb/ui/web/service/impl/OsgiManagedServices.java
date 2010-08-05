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
package org.openengsb.ui.web.service.impl;

import java.util.List;

import org.openengsb.core.config.ServiceManager;
import org.openengsb.ui.web.service.ManagedServices;
import org.osgi.framework.ServiceReference;

public class OsgiManagedServices implements ManagedServices {

    private List<ServiceManager> managedServices;
    private List<ServiceReference> managedServiceInstances;

    @Override
    public List<ServiceManager> getManagedServices() {
        return managedServices;
    }

    public void setManagedServices(List<ServiceManager> managedServices) {
        this.managedServices = managedServices;
    }

    public List<ServiceReference> getManagedServiceInstances() {
        return managedServiceInstances;
    }

    public void setManagedServiceInstances(List<ServiceReference> managedServiceInstances) {
        this.managedServiceInstances = managedServiceInstances;
    }
}
