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
package org.openengsb.connector.virtual.filewatcher.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.EventSupport;
import org.openengsb.core.api.VirtualConnectorProvider;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.common.SecurityAttributeProviderImpl;
import org.openengsb.core.common.internal.Activator;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.persistence.internal.DefaultConfigPersistenceService;
import org.openengsb.core.services.internal.ConnectorManagerImpl;
import org.openengsb.core.services.internal.ConnectorRegistrationManager;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyConfigPersistenceService;
import org.openengsb.core.test.NullDomain;
import org.osgi.framework.ServiceReference;

@RunWith(MockitoJUnitRunner.class)
public class FileWatcherConnectorTest extends AbstractOsgiMockServiceTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    protected ConnectorManager connectorManager;

    @Mock
    protected TransformationEngine transformationEngine;

    @Mock
    protected PersistInterface persistenceService;

    @Mock
    protected QueryInterface queryService;

    @Before
    public void setUp() throws Exception {
        setupConnectorManager();

        Activator activator = new Activator();
        activator.start(bundleContext);
        createDomainProviderMock(NullDomain.class, "example");

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("connector", "filewatcher");
        FileWatcherConnectorProvider provider = new FileWatcherConnectorProvider(persistenceService, queryService);
        provider.setId("filewatcher");
        registerService(provider, props, VirtualConnectorProvider.class);
    }

    private void setupConnectorManager() {
        ConnectorRegistrationManager serviceRegistrationManagerImpl = new ConnectorRegistrationManager(bundleContext,
                transformationEngine, null, new SecurityAttributeProviderImpl());
        serviceRegistrationManagerImpl.setBundleContext(bundleContext);

        ConnectorManagerImpl serviceManagerImpl = new ConnectorManagerImpl();
        serviceManagerImpl.setRegistrationManager(serviceRegistrationManagerImpl);
        serviceManagerImpl.setConfigPersistence(registerConfigPersistence());
        connectorManager = serviceManagerImpl;
    }

    private ConfigPersistenceService registerConfigPersistence() {
        DummyConfigPersistenceService<String> backend = new DummyConfigPersistenceService<String>();
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.CONFIGURATION_ID, Constants.CONFIG_CONNECTOR);
        props.put(Constants.BACKEND_ID, "dummy");
        DefaultConfigPersistenceService configPersistence = new DefaultConfigPersistenceService(backend);
        registerService(configPersistence, props, ConfigPersistenceService.class);
        return configPersistence;
    }

    @Test
    public void testConnectorProviderIsCreated() throws Exception {
        Collection<ServiceReference<ConnectorProvider>> serviceReferences =
                bundleContext.getServiceReferences(ConnectorProvider.class, "(domain=example)");
        assertThat(serviceReferences.isEmpty(), is(false));
    }

    @Test
    public void createFileWatcherConnector_shouldRegisterService() throws Exception {
        File testconnectorFolder = new File(tmpFolder.getRoot(), "testconnector");
        File testfile = new File(testconnectorFolder, "testfile");
        ConnectorDescription desc = makeConnectorDescription(CSVParser.class, testfile);
        connectorManager.create(desc);

        ServiceReference<NullDomain> serviceReference = bundleContext.getServiceReference(NullDomain.class);
        assertThat(serviceReference, not(nullValue()));
    }

    @Test
    public void createFileWatcherConnector_shouldCreateWatchDir() throws Exception {
        File testconnectorFolder = new File(tmpFolder.getRoot(), "testconnector");
        File testfile = new File(testconnectorFolder, "testfile");
        ConnectorDescription desc = makeConnectorDescription(CSVParser.class, testfile);
        connectorManager.create(desc);
        assertThat(testconnectorFolder.exists(), is(true));
    }

    @Test
    public void datachangedEvent_shouldTriggerUpdate() throws Exception {
        File testconnectorFolder = new File(tmpFolder.getRoot(), "testconnector");
        File testFile = new File(testconnectorFolder, "testfile");
        ConnectorDescription desc = makeConnectorDescription(CSVParser.class, testFile);
        connectorManager.create(desc);

        ServiceReference<EventSupport> reference = bundleContext.getServiceReference(EventSupport.class);
        EventSupport service = bundleContext.getService(reference);
        service.onEvent(new Event());
        verify(queryService).queryForActiveModels(TestModel.class);
    }

    private ConnectorDescription makeConnectorDescription(Class<?> parserClass, File testFile) {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("watchfile", testFile.getAbsolutePath());
        attributes.put("serializer", parserClass.getName());
        attributes.put("mixin.1", EventSupport.class.getName());
        attributes.put("modelType", TestModel.class.getName());
        return new ConnectorDescription("example", "filewatcher", attributes, new HashMap<String, Object>());
    }

    @Test
    public void modifyWatchedFile_shouldCallOnModified() throws Exception {
        File testconnectorFolder = new File(tmpFolder.getRoot(), "testconnector");
        File testfile = new File(testconnectorFolder, "testfile");
        ConnectorDescription desc = makeConnectorDescription(CSVParser.class, testfile);
        connectorManager.create(desc);
        FileUtils.write(testfile, "42,\"foo\",7");
        verify(persistenceService, timeout(2500)).commit(any(EKBCommit.class));
    }

}
