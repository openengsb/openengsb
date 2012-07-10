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
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.debugConfiguration;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Before;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.workflow.OsgiHelper;
import org.openengsb.labs.paxexam.karaf.options.ConfigurationPointer;
import org.openengsb.labs.paxexam.karaf.options.LogLevelOption.LogLevel;
import org.openengsb.labs.paxexam.karaf.options.configs.ManagementCfg;
import org.openengsb.labs.paxexam.karaf.options.configs.WebCfg;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractExamTestHelper {

    /*
     * to configure loglevel and debug-flag, create a file called itests.local.properties in src/test/resources. This
     * file should only contain simple properties. You can use debug=true and loglevel=INFO in this file. Additional
     * possible properties are debugport=5005 and hold=true. The debugport option specifies the port where the container
     * is reachable and the hold option if the container should wait for a debugger to be attached or not.
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExamTestHelper.class);

    private static final int DEBUG_PORT = 5005;
    private static final String LOG_LEVEL = "WARN";
    public static final long DEFAULT_TIMEOUT = 30000;

    @Inject
    private BundleContext bundleContext;

    @Before
    public void waitForRequiredTasks() throws Exception {
        RuleManager rm = getOsgiService(RuleManager.class);
        int count = 0;
        while (rm.getGlobalType("auditing") == null) {
            LOGGER.warn("waiting for auditing to finish init");
            Thread.sleep(1000);
            if (count++ > 100) {
                throw new IllegalStateException("auditing-config did not finish in time");
            }
        }
        count = 0;
        while (!rm.listImports().contains(OsgiHelper.class.getName())) {
            LOGGER.warn("waiting for auditing to finish init");
            Thread.sleep(1000);
            if (count++ > 100) {
                throw new IllegalStateException("auditing-config did not finish in time");
            }
        }
        count = 0;
        while (rm.get(new RuleBaseElementId(RuleBaseElementType.Process, "humantask")) == null) {
            LOGGER.warn("waiting for taskboxConfig to finish init");
            Thread.sleep(1000);
            if (count++ > 100) {
                throw new IllegalStateException("taskbox-config did not finish in time");
            }
        }
    }

    protected <T> T getOsgiService(Class<T> type, long timeout) {
        return getOsgiService(type, null, timeout);
    }

    protected <T> T getOsgiService(Class<T> type) {
        return getOsgiService(type, null, DEFAULT_TIMEOUT);
    }

    protected Bundle getInstalledBundle(String symbolicName) {
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals(symbolicName)) {
                return b;
            }
        }
        return null;
    }

    protected void waitForSiteToBeAvailable(String urlToWatchFor, Integer maxWaitTime) throws InterruptedException {
        Integer localCounter = maxWaitTime;
        while (localCounter != 0) {
            if (isUrlReachable(urlToWatchFor)) {
                return;
            }
            Thread.sleep(1000);
            localCounter--;
        }
        throw new IllegalStateException(String.format("Couldn't reach page %s within %s seconds", urlToWatchFor,
            maxWaitTime));
    }

    @SuppressWarnings("deprecation")
    protected boolean isUrlReachable(String url) {
        URL downloadUrl;
        InputStream is = null;
        DataInputStream dataInputStream;
        try {
            downloadUrl = new URL(url);
            is = downloadUrl.openStream();
            dataInputStream = new DataInputStream(new BufferedInputStream(is));
            while (dataInputStream.readLine() != null) {
                return true;
            }
        } catch (Exception e) {
            // well... what should we say; this could happen...
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
            }
        }
        return false;
    }

    protected <T> T getOsgiService(Class<T> type, String filter, long timeout) {
        ServiceTracker tracker = null;
        try {
            String flt;
            if (filter != null) {
                if (filter.startsWith("(")) {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")" + filter + ")";
                } else {
                    flt = "(&(" + Constants.OBJECTCLASS + "=" + type.getName() + ")(" + filter + "))";
                }
            } else {
                flt = "(" + Constants.OBJECTCLASS + "=" + type.getName() + ")";
            }
            Filter osgiFilter = FrameworkUtil.createFilter(flt);
            tracker = new ServiceTracker(bundleContext, osgiFilter, null);
            tracker.open(true);
            // Note that the tracker is not closed to keep the reference
            // This is buggy, as the service reference may change i think
            Object svc = type.cast(tracker.waitForService(timeout));
            if (svc == null) {
                @SuppressWarnings("rawtypes")
                Dictionary dic = bundleContext.getBundle().getHeaders();
                System.err.println("Test bundle headers: " + explode(dic));

                for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, null))) {
                    System.err.println("ServiceReference: " + ref);
                }

                for (ServiceReference ref : asCollection(bundleContext.getAllServiceReferences(null, flt))) {
                    System.err.println("Filtered ServiceReference: " + ref);
                }

                throw new RuntimeException("Gave up waiting for service " + flt);
            }
            return type.cast(svc);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter", e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    private static String explode(Dictionary dictionary) {
        Enumeration keys = dictionary.keys();
        StringBuffer result = new StringBuffer();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            result.append(String.format("%s=%s", key, dictionary.get(key)));
            if (keys.hasMoreElements()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /*
     * Provides an iterable collection of references, even if the original array is null
     */
    private static final Collection<ServiceReference> asCollection(ServiceReference[] references) {
        List<ServiceReference> result = new LinkedList<ServiceReference>();
        if (references != null) {
            for (ServiceReference reference : references) {
                result.add(reference);
            }
        }
        return result;
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    protected static String getWorkingDirectory() {
        return "target/paxrunner/features/";
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

    public static Option[] baseConfiguration() throws Exception {
        String loglevel = LOG_LEVEL;
        String debugPort = Integer.toString(DEBUG_PORT);
        boolean hold = true;
        boolean debug = false;
        InputStream paxLocalStream = ClassLoader.getSystemResourceAsStream("itests.local.properties");
        if (paxLocalStream != null) {
            Properties properties = new Properties();
            properties.load(paxLocalStream);
            loglevel = (String) ObjectUtils.defaultIfNull(properties.getProperty("loglevel"), loglevel);
            debugPort = (String) ObjectUtils.defaultIfNull(properties.getProperty("debugport"), debugPort);
            debug = ObjectUtils.equals(Boolean.TRUE.toString(), properties.getProperty("debug"));
            hold = ObjectUtils.equals(Boolean.TRUE.toString(), properties.getProperty("hold"));
        }
        Properties portNames = new Properties();
        InputStream portsPropertiesFile = ClassLoader.getSystemResourceAsStream("ports.properties");
        if (portsPropertiesFile == null) {
			throw new IllegalStateException("ports-configuration not found");
        }
        portNames.load(portsPropertiesFile);
        LOGGER.warn("running itests with the following port-config");
        LOGGER.warn(portNames.toString());
        LogLevel realLogLevel = transformLogLevel(loglevel);
        Option[] mainOptions =
            new Option[]{
                new VMOption("-Xmx2048m"),
                new VMOption("-XX:MaxPermSize=256m"),
                karafDistributionConfiguration().frameworkUrl(
                    maven().groupId("org.openengsb.framework").artifactId("openengsb-framework").type("zip")
                        .versionAsInProject()),
                logLevel(realLogLevel),
                editConfigurationFilePut(WebCfg.HTTP_PORT, (String) portNames.get("jetty.http.port")),
                editConfigurationFilePut(ManagementCfg.RMI_SERVER_PORT, (String) portNames.get("rmi.server.port")),
                editConfigurationFilePut(ManagementCfg.RMI_REGISTRY_PORT, (String) portNames.get("rmi.registry.port")),
                editConfigurationFilePut(new ConfigurationPointer("etc/org.openengsb.infrastructure.jms.cfg",
                    "openwire"), (String) portNames.get("jms.openwire.port")),
                editConfigurationFilePut(new ConfigurationPointer("etc/org.openengsb.infrastructure.jms.cfg",
                    "stomp"), (String) portNames.get("jms.stomp.port")),
                mavenBundle(maven().groupId("org.openengsb.wrapped").artifactId("net.sourceforge.htmlunit-all")
                    .versionAsInProject()) };
        if (debug) {
            return combine(mainOptions, debugConfiguration(debugPort, hold));
        }
        return mainOptions;
    }

    public String getConfigProperty(String config, String name) throws IOException {
        ConfigurationAdmin cm = getOsgiService(ConfigurationAdmin.class);
        Configuration configuration = cm.getConfiguration(config);
        return (String) configuration.getProperties().get(name);
    }

    private static LogLevel transformLogLevel(String logLevel) {
        if (logLevel.equals("ERROR")) {
            return LogLevel.ERROR;
        }
        if (logLevel.equals("WARN")) {
            return LogLevel.WARN;
        }
        if (logLevel.equals("INFO")) {
            return LogLevel.INFO;
        }
        if (logLevel.equals("DEBUG")) {
            return LogLevel.DEBUG;
        }
        if (logLevel.equals("TRACE")) {
            return LogLevel.TRACE;
        }
        return LogLevel.WARN;
    }
}
