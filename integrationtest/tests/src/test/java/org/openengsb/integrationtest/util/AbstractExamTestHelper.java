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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.ServiceManager;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.def.options.WorkingDirectoryOption;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
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

    @Before
    public void before() throws Exception {
        List<String> importantBundles = BaseExamConfiguration.getImportantBundleSymbolicNames();
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            for (String importantBundleSymbolicName : importantBundles) {
                if (!bundle.getSymbolicName().equals(importantBundleSymbolicName)) {
                    continue;
                }
                waitForActiveSpringService(bundle);
                break;
            }
        }
    }

    protected void waitForActiveSpringService(String bundleName) throws InterruptedException {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(bundleName)) {
                waitForActiveSpringService(bundle);
                return;
            }
        }
    }

    private void waitForActiveSpringService(Bundle bundle) throws InterruptedException {
        int times = 0;
        while (bundle.getState() != Bundle.ACTIVE) {
            if (times > 20) {
                Assert.fail(String.format("Bundle %s still not active", bundle.getSymbolicName()));
            }
            Thread.sleep(3000);
            times++;
        }
        ServiceTracker tracker =
            new ServiceTracker(bundle.getBundleContext(), "org.springframework.context.ApplicationContext", null);
        tracker.open();
        Object service = tracker.waitForService(60000);
        if (service == null) {
            Assert.fail(String.format("Bundle %s does not start spring service", bundle.getSymbolicName()));
        }
        tracker.close();
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        try {
            FileUtils.deleteDirectory(new File(getWorkingDirectory()));
        } catch (IOException e) {
            // yes we know about this, but this happens sometimes in windows...
        }
    }

    @AfterClass
    public static void afterClass() throws IOException {
        try {
            FileUtils.deleteDirectory(new File(getWorkingDirectory()));
        } catch (IOException e) {
            // ok, still not funny but lets try if the test passes if the file still exists
        }
    }

    @Configuration
    public static Option[] configuration() {
        List<Option> baseConfiguration = BaseExamConfiguration.getBaseExamOptions("../../");
        baseConfiguration
            .add(CoreOptions.systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"));
        baseConfiguration.add(new WorkingDirectoryOption(getWorkingDirectory()));
        BaseExamConfiguration.addEntireOpenEngSBPlatform(baseConfiguration);
        BaseExamConfiguration.addHtmlUnitTestDriver(baseConfiguration);
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

    protected ServiceManager retrieveServiceManager(BundleContext bundleContext, Class<? extends Domain> domain)
        throws InterruptedException, InvalidSyntaxException {
        String filter = "(domain=" + domain.getName() + ")";
        ServiceReference[] allServiceReferences =
            bundleContext.getAllServiceReferences(ServiceManager.class.getName(), filter);
        if (allServiceReferences != null) {
            for (ServiceReference serviceReference : allServiceReferences) {
                Object service = bundleContext.getService(serviceReference);
                return (ServiceManager) service;
            }
        }
        Assert.fail("No service manager found.");
        return null;
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
