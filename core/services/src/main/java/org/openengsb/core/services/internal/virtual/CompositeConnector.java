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
package org.openengsb.core.services.internal.virtual;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.VirtualConnector;
import org.osgi.framework.ServiceReference;

/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The AASTI licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

public class CompositeConnector extends VirtualConnector {

    private String queryString;
    private CompositeConnectorStrategy compositeHandler;

    public CompositeConnector(String instanceId) {
        super(instanceId);
    }

    @Override
    protected Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<ServiceReference> services = getOsgiServices();
        return compositeHandler.invoke(services, method, args);
    }

    @SuppressWarnings("unchecked")
    private List<ServiceReference> getOsgiServices() {
        OsgiUtilsService utilsService = OpenEngSBCoreServices.getServiceUtilsService();
        List<ServiceReference> references = utilsService.listServiceReferences(queryString);
        Collections.sort(references);
        return references;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setCompositeHandler(CompositeConnectorStrategy compositeHandler) {
        this.compositeHandler = compositeHandler;
    }

}
