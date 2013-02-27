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

package org.openengsb.core.ekb.persistence.persist.edb;

import org.junit.Before;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.common.EDBConverter;
import org.openengsb.core.ekb.persistence.persist.edb.internal.EOMode;
import org.openengsb.core.ekb.persistence.persist.edb.internal.EngineeringObjectEnhancer;

public abstract class AbstractEngineeringObjectEnhancerTest {
    public static final String CONTEXT_ID = "testcontext";
    private EOMode mode;
    protected EngineeringObjectEnhancer enhancer;
    
    protected AbstractEngineeringObjectEnhancerTest(EOMode mode) {
        this.mode = mode;
    }

    @Before
    public void setup() {
        EngineeringDatabaseService edbService = new TestEngineeringDatabaseService();
        EDBConverter edbConverter = new EDBConverter(edbService);
        TransformationEngine transformationEngine = new TestTransformationEngine();
        ModelRegistry modelRegistry = new TestModelRegistry();
        enhancer = new EngineeringObjectEnhancer(edbService, edbConverter, 
            transformationEngine, modelRegistry, mode.toString());
        ContextHolder.get().setCurrentContextId(CONTEXT_ID);
    }    

    protected EKBCommit getTestCommit() {
        return new EKBCommit().setDomainId("test").setConnectorId("test");
    }
}
