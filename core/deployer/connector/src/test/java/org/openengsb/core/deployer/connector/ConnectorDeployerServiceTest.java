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
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.deployer.connector.internal.DeployerStorage;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class ConnectorDeployerServiceTest extends AbstractOsgiMockServiceTest {

    private ConnectorDeployerService connectorDeployerService;
    private AuthenticationManager authManagerMock;
    private Authentication authMock;
    private ServiceManager serviceManagerMock;
    private DeployerStorage storageMock;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        connectorDeployerService = new ConnectorDeployerService();
        authManagerMock = mock(AuthenticationManager.class);
        authMock = mock(Authentication.class);
        serviceManagerMock = mock(ServiceManager.class);
        storageMock = mock(DeployerStorage.class);

        when(authManagerMock.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);
        registerService(serviceManagerMock, ServiceManager.class, "(connector=a-connector )");

        connectorDeployerService.setAuthenticationManager(authManagerMock);
        connectorDeployerService.setBundleContext(bundleContext);
        connectorDeployerService.setDeployerStorage(storageMock);

        FileUtils.touch(new File("example.connector"));
        FileUtils.touch(new File("other.txt"));
    }

    @After
    public void tearDown() {
        FileUtils.deleteQuietly(new File("example.connector"));
        FileUtils.deleteQuietly(new File("other.txt"));
    }

    @Test
    public void testConnectorFiles_shouldBeHandledByDeployer() {
        File connectorFile = new File("example.connector");

        assertThat(connectorDeployerService.canHandle(connectorFile), is(true));
    }

    @Test
    public void testUnknownFiles_shouldNotBeHandledByDeplyoer() {
        File otherFile = new File("other.txt");

        assertThat(connectorDeployerService.canHandle(otherFile), is(false));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testConnectorFile_souldBeInstalled() throws Exception {
        File connectorFile = new File("example.connector");
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
        File connectorFile = new File("example.connector");
        FileUtils.writeStringToFile(connectorFile, "connector=a-connector \n id=service-id \n a-key=a-value");
        MultipleAttributeValidationResult updateResult = mock(MultipleAttributeValidationResult.class);

        when(updateResult.isValid()).thenReturn(true);
        when(serviceManagerMock.update(anyString(), anyMap())).thenReturn(updateResult);

        connectorDeployerService.update(connectorFile);

        verify(serviceManagerMock).update(anyString(), argThat(new IsSomething()));
    }

    @Test
    public void testRemoveConnectorFile_shouldRemoveConnector() throws Exception {
        File connectorFile = new File("example.connector");

        when(storageMock.getConnectorType(connectorFile)).thenReturn("a-connector ");
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
