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

package org.openengsb.connector.maven.internal;

import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.domain.build.BuildDomainEvents;
import org.openengsb.domain.deploy.DeployDomainEvents;
import org.openengsb.domain.test.TestDomainEvents;

public class MavenServiceInstanceFactoryTest {

    @Test
    public void testCreatePlaintextReportService() throws Exception {
        MavenServiceInstanceFactory factory = new MavenServiceInstanceFactory();
        factory.setBuildEvents(mock(BuildDomainEvents.class));
        factory.setTestEvents(mock(TestDomainEvents.class));
        factory.setDeployEvents(mock(DeployDomainEvents.class));
        factory.setContextService(mock(ContextCurrentService.class));

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("projectPath", "someValue");
        MavenServiceImpl mavenService = factory.createServiceInstance("id", attributes);

        Assert.assertNotNull(mavenService);
    }
}
