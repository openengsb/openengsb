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

package org.openengsb.core.persistence.internal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.mockito.Mockito;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.persistence.PersistenceServiceTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class NeodatisPersistenceServiceTest extends PersistenceServiceTest {

    private NeodatisPersistenceService persistence;

    @Override
    protected PersistenceService createPersitenceService() throws Exception {
        BundleContext contextMock = Mockito.mock(BundleContext.class);
        Bundle bundleMock = Mockito.mock(Bundle.class);
        Mockito.when(bundleMock.getBundleContext()).thenReturn(contextMock);
        persistence = new NeodatisPersistenceService("target/db.data", bundleMock);
        return persistence;
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.forceDelete(new File("target/db.data"));
    }

}
