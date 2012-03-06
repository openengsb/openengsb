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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openengsb.core.persistence.test.objects.A;
import org.openengsb.core.persistence.test.objects.B;
import org.openengsb.core.persistence.test.objects.X;
import org.openengsb.core.persistence.test.objects.Z;
import org.openengsb.core.persistence.test.util.FileHelper;

public class DefaultPersistenceIndexTest {

    @Test
    public void testLoadingIndex_shouldCreateNewIndex() throws Exception {
        File tmpDir = FileHelper.createTempDirectory();
        new DefaultPersistenceIndex(tmpDir, new DefaultObjectPersistenceBackend());
        assertTrue(new File(tmpDir + "/index.ser").exists());
        FileUtils.deleteDirectory(tmpDir);
    }

    @Test
    public void testIndexObject_shouldPersistIndexForObjectAndMakeItRetrievableAgainFromLoadedIndex() throws Exception {
        File tmpDir = FileHelper.createTempDirectory();
        persistBean(tmpDir, B.class);
        DefaultPersistenceIndex defaultPersistenceIndex =
            new DefaultPersistenceIndex(tmpDir, new DefaultObjectPersistenceBackend());
        List<ObjectInfo> findIndexObject = defaultPersistenceIndex.findIndexObject(B.class);
        assertEquals(1, findIndexObject.size());
    }

    @Test
    public void testSearch_shouldAlsoFindObjectBySubclass() throws Exception {
        File tmpDir = FileHelper.createTempDirectory();
        persistBean(tmpDir, B.class);
        DefaultPersistenceIndex defaultPersistenceIndex =
            new DefaultPersistenceIndex(tmpDir, new DefaultObjectPersistenceBackend());
        List<ObjectInfo> findIndexObject = defaultPersistenceIndex.findIndexObject(A.class);
        assertEquals(1, findIndexObject.size());
    }

    @Test
    public void testSearch_shouldAlsoFindObjectByInterface() throws Exception {
        File tmpDir = FileHelper.createTempDirectory();
        persistBean(tmpDir, B.class);
        DefaultPersistenceIndex defaultPersistenceIndex =
            new DefaultPersistenceIndex(tmpDir, new DefaultObjectPersistenceBackend());
        List<ObjectInfo> findIndexObject = defaultPersistenceIndex.findIndexObject(Z.class);
        assertEquals(1, findIndexObject.size());
    }

    @Test
    public void testSearch_shouldAlsoFindObjectBySubInterface() throws Exception {
        File tmpDir = FileHelper.createTempDirectory();
        persistBean(tmpDir, B.class);
        DefaultPersistenceIndex defaultPersistenceIndex =
            new DefaultPersistenceIndex(tmpDir, new DefaultObjectPersistenceBackend());
        List<ObjectInfo> findIndexObject = defaultPersistenceIndex.findIndexObject(X.class);
        assertEquals(1, findIndexObject.size());
    }

    @Test
    public void testPersistingMultibleObjectsAtDifferentLvls_shouldReturnByA() throws Exception {
        File tmpDir = FileHelper.createTempDirectory();
        persistBean(tmpDir, B.class, A.class);
        DefaultPersistenceIndex defaultPersistenceIndex =
            new DefaultPersistenceIndex(tmpDir, new DefaultObjectPersistenceBackend());
        List<ObjectInfo> findIndexObject = defaultPersistenceIndex.findIndexObject(A.class);
        assertEquals(2, findIndexObject.size());
    }

    @Test
    public void testPersistingMultibleObjectsAtDifferentLvls_shouldReturnByB() throws Exception {
        File tmpDir = FileHelper.createTempDirectory();
        persistBean(tmpDir, B.class, A.class);
        DefaultPersistenceIndex defaultPersistenceIndex =
            new DefaultPersistenceIndex(tmpDir, new DefaultObjectPersistenceBackend());
        List<ObjectInfo> findIndexObject = defaultPersistenceIndex.findIndexObject(B.class);
        assertEquals(1, findIndexObject.size());
    }

    @Test
    public void testRemove_shouldAlsoRemoveObjectFromIndex() throws Exception {
        File tmpDir = FileHelper.createTempDirectory();
        persistBean(tmpDir, B.class, A.class);
        DefaultPersistenceIndex defaultPersistenceIndex =
            new DefaultPersistenceIndex(tmpDir, new DefaultObjectPersistenceBackend());
        List<ObjectInfo> findIndexObject = defaultPersistenceIndex.findIndexObject(A.class);
        assertEquals(2, findIndexObject.size());
        defaultPersistenceIndex.removeIndexObject(findIndexObject.get(0));
        defaultPersistenceIndex.updateIndex();
        findIndexObject = defaultPersistenceIndex.findIndexObject(A.class);
        assertEquals(1, findIndexObject.size());
    }

    private void persistBean(File index, Class<?>... objects) {
        DefaultPersistenceIndex defaultPersistenceIndex =
            new DefaultPersistenceIndex(index, new DefaultObjectPersistenceBackend());
        for (Class<?> class1 : objects) {
            defaultPersistenceIndex.indexObject(class1, new File(index + "/" + UUID.randomUUID()));
        }
        defaultPersistenceIndex.updateIndex();
    }

}
