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

package org.openengsb.integrationtest.util;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractExamTestHelper {

    @BeforeClass
    @AfterClass
    public static void setUp() throws IOException {
        String tmpDir = System.getProperty("java.io.tmpdir");
        String user = System.getProperty("user.name");
        String paxDir = String.format("/%spaxexam_runner_%s/", tmpDir, user);
        FileUtils.deleteDirectory(new File(paxDir));
    }

    protected <ServiceClass> ServiceClass retrieveService(BundleContext bundleContext,
            Class<? extends ServiceClass> serviceClass) throws InterruptedException {
        ServiceTracker tracker = new ServiceTracker(bundleContext, serviceClass.getName(), null);
        tracker.open();
        @SuppressWarnings("unchecked")
        ServiceClass service = (ServiceClass) tracker.waitForService(10000);
        tracker.close();
        Assert.assertNotNull(service);
        return service;
    }

    protected Version getBundleVersionBySymbolicName(String symbolicName, BundleContext context) {
        Bundle[] bundles = context.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(symbolicName)) {
                return bundle.getVersion();
            }
        }
        throw new RuntimeException("Buuuum");
    }
}
