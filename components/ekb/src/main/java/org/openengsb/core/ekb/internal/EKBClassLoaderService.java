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

package org.openengsb.core.ekb.internal;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.DelegationClassLoader;
import org.openengsb.labs.delegation.service.DelegationUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Simple class loading service for EKB internal usage. It uses the delegation class loading mechanisms to load models
 * and classes. The classes need to be provided by the labs delegation project under the model delegation context.
 */
public class EKBClassLoaderService implements EKBClassLoader {
    private static final long DEFAULT_TIMEOUT = 30000L;
    private long timeout = DEFAULT_TIMEOUT;
    private ClassLoader loader;
    private BundleContext context;

    public EKBClassLoaderService(BundleContext context) {
        this.context = context;
        loader = new DelegationClassLoader(context, Constants.DELEGATION_CONTEXT_MODELS,
            this.getClass().getClassLoader());
    }

    public EKBClassLoaderService() {
        loader = this.getClass().getClassLoader();
    }

    @Override
    public Class<?> loadClass(String classname) throws ClassNotFoundException {
        return loader.loadClass(classname);
    }

    @Override
    public Class<?> loadModel(ModelDescription model) throws ClassNotFoundException {
        Filter filter = DelegationUtil.createClassProviderFilter(Constants.DELEGATION_CONTEXT_MODELS,
            model.getModelClassName(), model.getModelVersion());
        ServiceTracker serviceTracker = new ServiceTracker(context, filter, null);
        serviceTracker.open();
        try {
            ClassProvider service = (ClassProvider) serviceTracker.waitForService(timeout);
            if (service == null) {
                throw new ClassNotFoundException(model.getModelClassName());
            }
            return service.loadClass(model.getModelClassName());
        } catch (InterruptedException e) {
            throw new ClassNotFoundException(model.getModelClassName(), e);
        } finally {
            serviceTracker.close();
        }
    }
}
