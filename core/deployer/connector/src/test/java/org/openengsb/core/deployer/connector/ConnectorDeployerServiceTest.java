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

package org.openengsb.core.deployer.connector;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentMatcher;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.deployer.connector.internal.DeployerStorage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class ConnectorDeployerServiceTest {

    private ConnectorDeployerService connectorDeployerService;
    private AuthenticationManager authManagerMock;
    private Authentication authMock;
    private BundleContext bundleContextMock;
    private List<ServiceReference> serviceReferenceMocks;
    private ServiceManager serviceManagerMock;
    private DeployerStorage storageMock;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    
    @Before
    public void setUp() throws Exception {
        connectorDeployerService = new ConnectorDeployerService();
        authManagerMock = mock(AuthenticationManager.class);
        authMock = mock(Authentication.class);
        bundleContextMock = mock(BundleContext.class);
        serviceReferenceMocks = Arrays.asList(mock(ServiceReference.class));
        serviceManagerMock = mock(ServiceManager.class);
        storageMock = mock(DeployerStorage.class);

        when(authManagerMock.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        when(bundleContextMock.getServiceReferences(anyString(), anyString())).thenReturn(
                serviceReferenceMocks.toArray(new ServiceReference[0]));
        when(bundleContextMock.getService(any(ServiceReference.class))).thenReturn(serviceManagerMock);

        connectorDeployerService.setAuthenticationManager(authManagerMock);
        connectorDeployerService.setBundleContext(bundleContextMock);
        connectorDeployerService.setDeployerStorage(storageMock);
    }

    @Test
    public void testConnectorFiles_shouldBeHandledByDeployer() throws IOException {
        File connectorFile = temporaryFolder.newFile("example.connector");

        assertThat(connectorDeployerService.canHandle(connectorFile), is(true));
    }

    @Test
    public void testUnknownFiles_shouldNotBeHandledByDeplyoer() throws IOException {
        File otherFile = temporaryFolder.newFile("other.txt");

        assertThat(connectorDeployerService.canHandle(otherFile), is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConnectorFile_shouldBeInstalled() throws Exception {
        File connectorFile = temporaryFolder.newFile("example.connector");
        FileUtils.writeStringToFile(connectorFile, "connector=a-connector \n id=service-id \n a-key=a-value");
        MultipleAttributeValidationResult updateResult = mock(MultipleAttributeValidationResult.class);

        when(updateResult.isValid()).thenReturn(true);
        when(serviceManagerMock.update(anyString(), anyMap())).thenReturn(updateResult);

        connectorDeployerService.install(connectorFile);

        verify(serviceManagerMock).update(anyString(), argThat(new IsSomething()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateConnectorFile_shouldBeUpdated() throws Exception {
        File connectorFile = temporaryFolder.newFile("example.connector");
        FileUtils.writeStringToFile(connectorFile, "connector=a-connector \n id=service-id \n a-key=a-value");
        MultipleAttributeValidationResult updateResult = mock(MultipleAttributeValidationResult.class);

        when(updateResult.isValid()).thenReturn(true);
        when(serviceManagerMock.update(anyString(), anyMap())).thenReturn(updateResult);

        connectorDeployerService.update(connectorFile);

        verify(serviceManagerMock).update(anyString(), argThat(new IsSomething()));
    }

    @Test
    public void testRemoveConnectorFile_shouldRemoveConnector() throws Exception {
        File connectorFile = temporaryFolder.newFile("example.connector");

        when(storageMock.getServiceId(any(File.class))).thenReturn("service-id");

        connectorDeployerService.uninstall(connectorFile);

        verify(serviceManagerMock).delete("service-id");
    }

    class IsSomething extends ArgumentMatcher<Map<String, String>> {
        @Override
        public boolean matches(Object o) {
            return true;
        }
    }

}
