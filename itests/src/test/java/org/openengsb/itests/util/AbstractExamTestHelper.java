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

package org.openengsb.itests.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.waitForFrameworkStartup;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.vmOption;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.workingDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.karaf.testing.AbstractIntegrationTest;
import org.apache.karaf.testing.Helper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractExamTestHelper extends AbstractIntegrationTest {

    private static final String SYS_PROP_LOG = "itests_log";
    private static final String LOG_LEVEL = "WARN";

    public enum SetupType {
            BLUEPRINT, SPRING, START_ONLY
    }

    @Inject
    private BundleContext bundleContext;

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    protected static String getWorkingDirectory() {
        return "target/paxrunner/features/";
    }

    @Before
    public void before() throws Exception {
        List<String> importantBundles = getImportantBundleSymbolicNames();
        for (String bundle : importantBundles) {
            waitForBundle(bundle, SetupType.BLUEPRINT);
        }
        waitForBundle("org.openengsb.ui.web", SetupType.SPRING);
        authenticateAsAdmin();
    }

    public static List<String> getImportantBundleSymbolicNames() {
        List<String> importantBundles = new ArrayList<String>();
        importantBundles.add("org.openengsb.core.common");
        importantBundles.add("org.openengsb.core.persistence");
        importantBundles.add("org.openengsb.core.workflow");
        importantBundles.add("org.openengsb.core.security");
        return importantBundles;
    }

    protected void authenticateAsAdmin() throws InterruptedException {
        authenticate("admin", "password");
    }

    protected void authenticate(String user, String password) throws InterruptedException {
        AuthenticationManager authenticationManager = getOsgiService(AuthenticationManager.class, 20000);
        Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user, password));
        assertThat(authentication.isAuthenticated(), is(true));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    protected void waitForBundle(String bundleName, SetupType setupType) throws InterruptedException {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(bundleName)) {
                waitForBundle(bundle, setupType);
                return;
            }
        }
    }

    private void waitForBundle(Bundle bundle, SetupType setupType) throws InterruptedException {
        waitForBundleActivation(bundle);
        if (SetupType.SPRING.equals(setupType)) {
            waitForSpring(bundle);
        } else if (SetupType.BLUEPRINT.equals(setupType)) {
            waitForBlueprint(bundle);
        }
    }

    private void waitForBundleActivation(Bundle bundle) throws InterruptedException {
        int times = 0;
        while (bundle.getState() != Bundle.ACTIVE) {
            if (times > 20) {
                Assert.fail(String.format("Bundle %s still not active", bundle.getSymbolicName()));
            }
            Thread.sleep(3000);
            times++;
        }
    }

    private void waitForSpring(Bundle bundle) throws InterruptedException {
        ServiceTracker tracker =
            new ServiceTracker(bundle.getBundleContext(), "org.springframework.context.ApplicationContext", null);
        tracker.open();
        Object service = tracker.waitForService(60000);
        if (service == null) {
            Assert.fail(String.format("Bundle %s does not start spring service", bundle.getSymbolicName()));
        }
        tracker.close();
    }

    private void waitForBlueprint(Bundle bundle) throws InterruptedException {
        ServiceTracker tracker =
            new ServiceTracker(bundle.getBundleContext(), "org.osgi.service.blueprint.container.BlueprintContainer",
                null);
        tracker.open();
        Object service = tracker.waitForService(60000);
        if (service == null) {
            Assert.fail(String.format("Bundle %s does not start blueprint service", bundle.getSymbolicName()));
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
    public static Option[] configuration() throws Exception {
        Option[] baseOptions = Helper.getDefaultOptions();
        String logLvl = System.getProperty(SYS_PROP_LOG) != null ? System.getProperty(SYS_PROP_LOG) : LOG_LEVEL;
        return combine(
            baseOptions,
            Helper.loadKarafFeatures("config", "management"),
            Helper.setLogLevel(logLvl),
            // Helper.activateDebugging("5005"),
            mavenBundle(maven().groupId("org.apache.aries.blueprint").artifactId("org.apache.aries.blueprint")
                .versionAsInProject()),
            scanFeatures(maven().groupId("org.openengsb").artifactId("openengsb").type("xml").classifier("features")
                .versionAsInProject(), "openengsb-core"), workingDirectory(getWorkingDirectory()),
            vmOption("-Dorg.osgi.framework.system.packages.extra=sun.reflect"),
            vmOption("-Dorg.osgi.service.http.port=8090"), waitForFrameworkStartup(),
            mavenBundle(maven().groupId("org.openengsb.wrapped").artifactId("net.sourceforge.htmlunit-all")
                .versionAsInProject()), felix());
    }

}
