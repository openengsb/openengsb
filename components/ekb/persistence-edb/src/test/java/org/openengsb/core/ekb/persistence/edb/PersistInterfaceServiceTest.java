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

package org.openengsb.core.ekb.persistence.edb;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.persistence.edb.internal.EDBConverter;
import org.openengsb.core.ekb.persistence.edb.internal.PersistInterfaceService;
import org.openengsb.core.ekb.persistence.edb.models.TestModel2;

public class PersistInterfaceServiceTest {
    private PersistInterfaceService service;
    
    @Before
    public void setUp() {
        this.service = new PersistInterfaceService();
        EngineeringDatabaseService edbService = mock(EngineeringDatabaseService.class);
        
        service.setEdbService(edbService);
        EDBConverter converter = new EDBConverter();
        converter.setEdbService(edbService);
        service.setEdbConverter(converter);
    }
    
    @Test
    public void testIfRealModelsCanBeCommited_shouldWork() throws Exception {
        EKBCommit commit = new EKBCommit();
        commit.setDomainId("testdomain").setConnectorId("testconnector").setInstanceId("testinstance");
        commit.addInsert(new TestModel2());
        service.commit(commit);
    }
}
