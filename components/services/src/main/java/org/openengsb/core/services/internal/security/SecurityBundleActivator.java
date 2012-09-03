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

package org.openengsb.core.services.internal.security;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class SecurityBundleActivator implements BundleActivator {

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void start(BundleContext context) throws Exception {
        DefaultOsgiUtilsService utilsService = new DefaultOsgiUtilsService(context);
        EntryUtils.setUtilsService(utilsService);
        RootSubjectHolder.init();
        executor.submit(new UserDataInitializer(utilsService));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        executor.shutdownNow();
    }

}
