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

package org.openengsb.core.ekb.modelregistry.tracker.internal;

import java.util.Hashtable;

import org.openengsb.core.ekb.api.ModelGraph;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.BundleTracker;

/**
 * Activator for the EKB bundle. It adds the model registry as bundle listener
 */
public class Activator implements BundleActivator {
    private BundleTracker tracker;

    @Override
    public void start(BundleContext context) throws Exception {
        ModelGraph graph = context.getService(context.getServiceReference(ModelGraph.class));
        ModelRegistryService service = new ModelRegistryService(context);
        service.setGraphDb(graph);
        service.setEkbClassLoader(new EKBClassLoaderService(context));
        tracker = service;
        tracker.open();
        context.registerService(ModelRegistry.class, service, new Hashtable<String, Object>());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        tracker.close();
    }

}
