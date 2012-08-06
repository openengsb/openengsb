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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.persistence.test.objects.A;
import org.openengsb.core.persistence.test.objects.B;
import org.openengsb.core.persistence.test.util.FileHelper;

import com.google.common.collect.Lists;

public class DefaultPersistenceServiceTest {
    private File tmpDir;
    DefaultPersistenceIndex persistenceIndex;
    DefaultPersistenceService persistenceService;

    @Before
    public void setUp() throws Exception {
        tmpDir = FileHelper.createTempDirectory();
        persistenceIndex = new DefaultPersistenceIndex(tmpDir, new DefaultObjectPersistenceBackend());
        persistenceService =
            new DefaultPersistenceService(tmpDir, new DefaultObjectPersistenceBackend(), persistenceIndex);
    }

    @Test
    public void testPersistingAndLoadingObject_shouldWork() throws Exception {
        persistenceService.create(new B());
        List<B> query = persistenceService.query(new B());
        assertEquals(1, query.size());
    }

    @Test
    public void testPersistingAndLoadingObject_shouldChangeAfterworks() throws Exception {
        persistenceService.create(new B());
        List<B> query = persistenceService.query(new B());
        assertEquals(1, query.size());
        assertEquals("lala", query.get(0).getAdditionalValue());
    }

    @Test
    public void testPersistingAndLoadingVariousObject_shouldWork() throws Exception {
        persistenceService.create(new B());
        persistenceService.create(new A());
        List<A> query = persistenceService.query(new A());
        assertEquals(2, query.size());
    }

    @Test
    public void testQueryBySample_shouldReturnCorrectObject() throws Exception {
        persistenceService.create(new B("other"));
        persistenceService.create(new A("blub"));
        List<A> query = persistenceService.query(new A("blub"));
        assertEquals(1, query.size());
    }

    @Test
    public void testPersistingAndLoadingVariousMultibleObjects() throws Exception {
        persistenceService.create(Lists.newArrayList(new B(), new A()));
        List<B> query = persistenceService.query(new B());
        assertEquals(1, query.size());
    }

    @Test
    public void testUpdatingAndRetrievingUpdatedObjects_shouldReturnUpdatedObjects() throws Exception {
        persistenceService.create(Lists.newArrayList(new B(), new A()));
        List<B> query = persistenceService.query(new B("blub"));
        assertEquals(0, query.size());
        persistenceService.update(new B(), new B("blub"));
        query = persistenceService.query(new B("blub"));
        assertEquals(1, query.size());
    }

    @Test
    public void testDeleteObject_shouldRemoveItFromDatabase() throws Exception {
        persistenceService.create(Lists.newArrayList(new B("blub"), new A()));
        List<B> query = persistenceService.query(new B("blub"));
        assertEquals(1, query.size());
        persistenceService.delete(new B("blub"));
        query = persistenceService.query(new B("blub"));
        assertEquals(0, query.size());
    }
}
