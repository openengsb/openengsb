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

package org.openengsb.core.weaver.service.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

/**
 * The activator of the weaver service bundle. Initiates the model weaver service and (un)register it.
 */
public class Activator implements BundleActivator {
    private ServiceRegistration<?> weavingHookService;

    @Override
    public void start(BundleContext context) throws Exception {
        ModelWeaver service = new ModelWeaver(context);
        weavingHookService = context.registerService(WeavingHook.class.getName(), service, null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        weavingHookService.unregister();
    }
}
