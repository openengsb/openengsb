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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.labs.paxexam.karaf.options.LogLevelOption.LogLevel;
import org.openengsb.labs.paxexam.karaf.options.configs.ManagementCfg;
import org.openengsb.labs.paxexam.karaf.options.configs.WebCfg;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.util.tracker.ServiceTracker;
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

    private static final int DEBUG_PORT = 5005;
    private static final String LOG_LEVEL = "WARN";
    protected static final String WEBUI_PORT = "8091";
    protected static final String RMI_REGISTRY_PORT = "1100";
    protected static final String RMI_SERVER_PORT = "44445";
    public static final long DEFAULT_TIMEOUT = 30000;

    @Inject
    private BundleContext bundleContext;

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
        importantBundles.add("org.openengsb.core.api");
        importantBundles.add("org.openengsb.core.common");
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
        LogLevel realLogLevel = transformLogLevel(loglevel);
        Option[] mainOptions =
            new Option[]{
                karafDistributionConfiguration().frameworkUrl(
                    maven().groupId("org.openengsb").artifactId("openengsb").type("zip").versionAsInProject()),
                logLevel(realLogLevel),
                editConfigurationFilePut(WebCfg.HTTP_PORT, WEBUI_PORT),
                editConfigurationFilePut(ManagementCfg.RMI_SERVER_PORT, RMI_SERVER_PORT),
                editConfigurationFilePut(ManagementCfg.RMI_REGISTRY_PORT, RMI_REGISTRY_PORT),
                mavenBundle(maven().groupId("org.openengsb.wrapped").artifactId("net.sourceforge.htmlunit-all")
                    .versionAsInProject()) };
        if (debug) {
            return combine(mainOptions, debugConfiguration(debugPort, hold));
        }
        return mainOptions;
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
