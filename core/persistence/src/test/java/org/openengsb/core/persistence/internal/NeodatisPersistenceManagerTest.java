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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.api.persistence.PersistenceService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

public class NeodatisPersistenceManagerTest {

    private NeodatisPersistenceManager persistenceManager;

    private Bundle bundleMock;

    @Before
    public void setUp() {
        persistenceManager = new NeodatisPersistenceManager();
        persistenceManager.setPersistenceRootDir("target/data");
        bundleMock = Mockito.mock(Bundle.class);
        Mockito.when(bundleMock.getSymbolicName()).thenReturn("testBundle");
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        Mockito.when(bundleMock.getBundleContext()).thenReturn(bundleContext);
        Version versionMock = Mockito.mock(Version.class);
        Mockito.when(bundleMock.getVersion()).thenReturn(versionMock);
    }

    @Test
    public void getPersistenceForBundle_shouldCreatePersistenceService() {
        PersistenceService persistenceService = persistenceManager.getPersistenceForBundle(bundleMock);
        assertThat(persistenceService, notNullValue());
    }

    @Test
    public void getPersistenceForBundleTwice_shouldReturnSamePersistenceService() {
        PersistenceService first = persistenceManager.getPersistenceForBundle(bundleMock);
        PersistenceService second = persistenceManager.getPersistenceForBundle(bundleMock);
        assertThat(second == first, is(true));
    }

}
