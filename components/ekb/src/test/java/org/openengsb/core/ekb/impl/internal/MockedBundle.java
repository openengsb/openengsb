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

package org.openengsb.core.ekb.impl.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

public class MockedBundle implements Bundle {
    private boolean full;

    public MockedBundle(boolean full) {
        this.full = full;
    }

    @Override
    public int compareTo(Bundle o) {
        return 0;
    }

    @Override
    public <A> A adapt(Class<A> arg0) {
        return null;
    }

    @Override
    public Enumeration<URL> findEntries(String arg0, String arg1, boolean arg2) {
        Vector<URL> classes = new Vector<URL>();
        if (full) {
            try {
                classes.add(new URL("file:/org/openengsb/core/ekb/impl/internal/models/ModelA.class"));
                classes.add(new URL("file:/org/openengsb/core/ekb/impl/internal/models/TestModel.class"));
            } catch (MalformedURLException e) {
                System.err.println("Malformed URL Exception");
            }
        }
        return classes.elements();
    }

    @Override
    public BundleContext getBundleContext() {
        return null;
    }

    @Override
    public long getBundleId() {
        return 0;
    }

    @Override
    public File getDataFile(String arg0) {
        return null;
    }

    @Override
    public URL getEntry(String arg0) {
        return null;
    }

    @Override
    public Enumeration<String> getEntryPaths(String arg0) {
        return null;
    }

    @Override
    public Dictionary<String, String> getHeaders() {
        return null;
    }

    @Override
    public Dictionary<String, String> getHeaders(String arg0) {
        return null;
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public String getLocation() {
        return null;
    }

    @Override
    public ServiceReference<?>[] getRegisteredServices() {
        return null;
    }

    @Override
    public URL getResource(String arg0) {
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String arg0) throws IOException {
        return null;
    }

    @Override
    public ServiceReference<?>[] getServicesInUse() {
        return null;
    }

    @Override
    public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int arg0) {
        return null;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public String getSymbolicName() {
        return null;
    }

    @Override
    public Version getVersion() {
        return new Version(1, 0, 0);
    }

    @Override
    public boolean hasPermission(Object arg0) {
        return false;
    }

    @Override
    public Class<?> loadClass(String arg0) throws ClassNotFoundException {
        return this.getClass().getClassLoader().loadClass(arg0);
    }

    @Override
    public void start() throws BundleException {
    }

    @Override
    public void start(int arg0) throws BundleException {
    }

    @Override
    public void stop() throws BundleException {
    }

    @Override
    public void stop(int arg0) throws BundleException {
    }

    @Override
    public void uninstall() throws BundleException {
    }

    @Override
    public void update() throws BundleException {
    }

    @Override
    public void update(InputStream arg0) throws BundleException {
    }

}
