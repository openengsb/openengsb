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
package org.openengsb.framework.edbi.hook.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.edbi.api.IndexCommit;
import org.openengsb.core.ekb.api.EKBCommit;

public class CommitConverterTest {

    private CommitConverter converter;

    @Before
    public void setUp() throws Exception {
        // mock auth context
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContext.getAuthenticatedPrincipal()).thenReturn("testUser");

        // mock context
        ContextHolder contextHolder = ContextHolder.get();
        contextHolder.setCurrentContextId("testContext");

        // commit converter
        converter = new CommitConverter(authenticationContext, contextHolder);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void convert_convertsCorrectly() throws Exception {
        // build test commit
        EKBCommit commit = new EKBCommit();
        commit.setConnectorId("testConnector");
        commit.setDomainId("testDomain");
        commit.setInstanceId("testInstance");
        commit.setRevisionNumber(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));

        OpenEngSBModel insert1 = new TestModelA();
        OpenEngSBModel insert2 = new TestModelA();
        OpenEngSBModel insert3 = new TestModelB();

        OpenEngSBModel update1 = new TestModelA();
        OpenEngSBModel update2 = new TestModelA();

        OpenEngSBModel delete1 = new TestModelA();
        OpenEngSBModel delete2 = new TestModelB();

        commit.addInsert(insert1).addInsert(insert2).addInsert(insert3);
        commit.addUpdate(update1).addUpdate(update2);
        commit.addDelete(delete1).addDelete(delete2);

        // test
        IndexCommit convertedCommit = converter.convert(commit);

        // assert
        assertEquals("testConnector", convertedCommit.getConnectorId());
        assertEquals("testDomain", convertedCommit.getDomainId());
        assertEquals("testInstance", convertedCommit.getInstanceId());
        assertEquals("550e8400-e29b-41d4-a716-446655440000", convertedCommit.getCommitId().toString());

        assertEquals("testContext", convertedCommit.getContextId());
        assertEquals("testUser", convertedCommit.getUser());

        Map<Class<?>, List<OpenEngSBModel>> inserts = convertedCommit.getInserts();
        assertEquals(2, inserts.size());
        assertEquals(2, inserts.get(TestModelA.class).size());
        assertEquals(1, inserts.get(TestModelB.class).size());

        assertSame(insert1, inserts.get(TestModelA.class).get(0));
        assertSame(insert2, inserts.get(TestModelA.class).get(1));
        assertSame(insert3, inserts.get(TestModelB.class).get(0));

        Map<Class<?>, List<OpenEngSBModel>> updates = convertedCommit.getUpdates();
        assertEquals(1, updates.size());
        assertEquals(2, updates.get(TestModelA.class).size());

        assertSame(update1, updates.get(TestModelA.class).get(0));
        assertSame(update2, updates.get(TestModelA.class).get(1));

        Map<Class<?>, List<OpenEngSBModel>> deletes = convertedCommit.getDeletes();
        assertEquals(2, deletes.size());
        assertEquals(1, deletes.get(TestModelA.class).size());
        assertEquals(1, deletes.get(TestModelB.class).size());

        assertSame(delete1, deletes.get(TestModelA.class).get(0));
        assertSame(delete2, deletes.get(TestModelB.class).get(0));

        Set<Class<?>> modelClasses = convertedCommit.getModelClasses();
        assertEquals(2, modelClasses.size());

        assertTrue("Model classes do not contain TestModel_A", modelClasses.contains(TestModelA.class));
        assertTrue("Model classes do not contain TestModel_B", modelClasses.contains(TestModelB.class));
    }

    @After
    public void tearDown() throws Exception {
        converter = null;
    }

    private static class TestModelA extends OpenEngSBModelStub {

    }

    private static class TestModelB extends OpenEngSBModelStub {

    }

    private abstract static class OpenEngSBModelStub implements OpenEngSBModel {

        @Override
        public List<OpenEngSBModelEntry> toOpenEngSBModelValues() {
            return null;
        }

        @Override
        public List<OpenEngSBModelEntry> toOpenEngSBModelEntries() {
            return null;
        }

        @Override
        public Object retrieveInternalModelId() {
            return null;
        }

        @Override
        public String retrieveInternalModelIdName() {
        	return null;
        }
        
        @Override
        public Long retrieveInternalModelTimestamp() {
            return null;
        }

        @Override
        public Integer retrieveInternalModelVersion() {
            return null;
        }

        @Override
        public void addOpenEngSBModelEntry(OpenEngSBModelEntry entry) {

        }

        @Override
        public void removeOpenEngSBModelEntry(String key) {

        }

        @Override
        public List<OpenEngSBModelEntry> getOpenEngSBModelTail() {
            return null;
        }

        @Override
        public void setOpenEngSBModelTail(List<OpenEngSBModelEntry> entries) {

        }

        @Override
        public String retrieveModelName() {
            return null;
        }

        @Override
        public String retrieveModelVersion() {
            return null;
        }
    }
}
