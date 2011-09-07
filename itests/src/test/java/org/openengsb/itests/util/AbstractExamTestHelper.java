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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.karaf.testing.AbstractIntegrationTest;
import org.apache.karaf.testing.Helper;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractExamTestHelper extends AbstractIntegrationTest {

    /*
     * to configure loglevel and debug-flag, create a file called itests.local.properties in src/test/resources. This
     * file should only contain simple properties. You can use debug=true and loglevel=INFO in this file.
     */

    private static final long DEFAULT_TIMEOUT = 40000;
    private static final int DEBUG_PORT = 5005;
    protected static final int WEBUI_PORT = 8091;
    protected static final int RMI_REGISTRY_PORT = 1100;
    protected static final int RMI_SERVER_PORT = 44445;

    private boolean isBeforeExecuted = false;

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
        if (isBeforeExecuted) {
            // Fix for test runner bug
            return;
        }
        isBeforeExecuted = true;

        List<String> importantBundles = getImportantBundleSymbolicNames();
        for (String bundle : importantBundles) {
            waitForBundle(bundle, SetupType.BLUEPRINT);
        }

        registerConfigPersistence("contextFilePersistenceService", "CONTEXT");
        registerConfigPersistence("workflow-persistence", "WORKFLOW");
        registerConfigPersistence("persistenceService", "CONNECTOR");

        waitForBundle("org.openengsb.ui.admin", SetupType.SPRING);
    }

    public void registerConfigPersistence(String backendId, String configurationId) throws ConfigurationException {
        ManagedServiceFactory factoryService =
            getOsgiService(ManagedServiceFactory.class, "(service.pid=org.openengsb.persistence.config)",
                30000);
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        String pid = "org.openengsb.persistence.config." + UUID.randomUUID();
        props.put("backend.id", backendId);
        props.put("configuration.id", configurationId);
        props.put("service.pid", pid);
        factoryService.updated(pid, props);
    }

    public static List<String> getImportantBundleSymbolicNames() {
        List<String> importantBundles = new ArrayList<String>();
        importantBundles.add("org.openengsb.infrastucture.jpa");
        importantBundles.add("org.openengsb.core.api");
        importantBundles.add("org.openengsb.core.common");
        importantBundles.add("org.openengsb.core.edb");
        importantBundles.add("org.openengsb.core.ekb");
        importantBundles.add("org.openengsb.core.service");
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

    @Override
    protected <T> T getOsgiService(Class<T> type) {
        return getOsgiService(type, null, DEFAULT_TIMEOUT);
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        try {
            FileUtils.deleteDirectory(new File(getWorkingDirectory()));
        } catch (IOException e) {
            // yes we know about this, but this happens sometimes in windows...
        }
        if (new File("log4j.properties.local").exists()) {
            LogManager.resetConfiguration();
            PropertyConfigurator.configure("log4j.properties.local");
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
        /*
         * do not change the values for loglevel or debug here, but refer the comment at the top of the class for
         * further instructions
         */
        String loglevel = "WARN";
        boolean debug = false;
        InputStream paxLocalStream = ClassLoader.getSystemResourceAsStream("itests.local.properties");
        if (paxLocalStream != null) {
            Properties properties = new Properties();
            properties.load(paxLocalStream);
            loglevel = (String) ObjectUtils.defaultIfNull(properties.getProperty("loglevel"), loglevel);
            debug = ObjectUtils.equals(Boolean.TRUE.toString(), properties.getProperty("debug"));
        }

        if (debug) {
            baseOptions = combine(baseOptions, Helper.activateDebugging(Integer.toString(DEBUG_PORT)));
        }
        String targetpath = new File("target").getAbsolutePath();
        FileUtils.deleteDirectory(new File(targetpath, "karaf.data"));
        return combine(
            baseOptions,
            Helper.loadKarafStandardFeatures("config", "management"),
            Helper.setLogLevel(loglevel),
            mavenBundle(maven().groupId("org.apache.aries").artifactId("org.apache.aries.util")
                .versionAsInProject()),
            mavenBundle(maven().groupId("org.apache.aries.proxy").artifactId("org.apache.aries.proxy")
                .versionAsInProject()),
            mavenBundle(maven().groupId("org.apache.aries.blueprint").artifactId("org.apache.aries.blueprint")
                .versionAsInProject()),
            mavenBundle(maven().groupId("org.apache.felix").artifactId("org.apache.felix.configadmin")
                        .versionAsInProject()),
            scanFeatures(
                maven().groupId("org.openengsb").artifactId("openengsb").type("xml").classifier("features-itests")
                    .versionAsInProject(), "openengsb-connector-memoryauditing", "openengsb-ui-admin"),
            workingDirectory(getWorkingDirectory()),
            vmOption("-Dorg.osgi.framework.system.packages.extra=com.sun.org.apache.xerces.internal.dom," 
                + "com.sun.org.apache.xerces.internal.jaxp,org.apache.karaf.branding,sun.reflect"),
            vmOption("-Dorg.osgi.service.http.port=" + WEBUI_PORT), vmOption("-DrmiRegistryPort="
                    + RMI_REGISTRY_PORT), vmOption("-DrmiServerPort=" + RMI_SERVER_PORT),
            waitForFrameworkStartup(),
            vmOption("-Dkaraf.data=" + targetpath + "/karaf.data"),
            vmOption("-Dkaraf.home=" + targetpath + "/karaf.home"),
            vmOption("-Dkaraf.base=" + targetpath + "/karaf.base"),
            mavenBundle(maven().groupId("org.openengsb.wrapped").artifactId("net.sourceforge.htmlunit-all")
                .versionAsInProject()), felix());
    }

}
