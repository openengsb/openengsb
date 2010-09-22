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
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.def.options.WorkingDirectoryOption;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;

public abstract class AbstractExamTestHelper {

    @Inject
    private BundleContext bundleContext;

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    protected static String getWorkingDirectory() {
        return System.getProperty("java.io.tmpdir") + "/paxexam_runner_" + System.getProperty("user.name");
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        FileUtils.deleteDirectory(new File(getWorkingDirectory()));
    }

    @Configuration
    public static Option[] configuration() {
        List<Option> baseConfiguration = BaseExamConfiguration.getBaseExamOptions("../");
        baseConfiguration
                .add(CoreOptions.systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"));
        baseConfiguration.add(new WorkingDirectoryOption(getWorkingDirectory()));
        BaseExamConfiguration.addEntireOpenEngSBPlatform(baseConfiguration);
        Option[] options = BaseExamConfiguration.convertOptionListToArray(baseConfiguration);
        return CoreOptions.options(options);
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
