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

package org.openengsb.core.edb.jpa.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.CommitMetaInfo;
import org.openengsb.core.api.model.CommitQueryRequest;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBObjectEntry;

public class EDBQueryTest extends AbstractEDBTest {

    @Test
    public void testQueryWithSomeAspects_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("A", "B", data1);
        putValue("Cow", "Milk", data1);
        putValue("Dog", "Food", data1);
        EDBObject v1 = new EDBObject("/test/query1", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v1);
        long time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data2 = new HashMap<String, EDBObjectEntry>();
        putValue("Cow", "Milk", data2);
        putValue("House", "Garden", data2);
        v1 = new EDBObject("/test/query2", data2);
        ci = getEDBCommit();
        ci.insert(v1);
        long time2 = db.commit(ci);

        QueryRequest req = QueryRequest.query("A", "B");
        List<EDBObject> list1 = db.query(req);
        req = QueryRequest.query("A", "B").addParameter("Dog", "Food");
        List<EDBObject> list2 = db.query(req);
        req = QueryRequest.query("Cow", "Milk");
        List<EDBObject> list3 = db.query(req);
        req = QueryRequest.query("A", "B").addParameter("Cow", "Milk")
                .addParameter("House", "Garden");
        List<EDBObject> list4 = db.query(req);
        
        assertThat(list1.size(), is(1));
        assertThat(list2.size(), is(1));
        assertThat(list3.size(), is(2));
        assertThat(list4.size(), is(0));

        checkTimeStamps(Arrays.asList(time1, time2));
    }

    @Test
    public void testQueryOfOldVersion_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1v1 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 1", data1v1);
        putValue("pre:KeyB", "pre:Value A 1", data1v1);
        EDBObject v11 = new EDBObject("pre:/test/object1", data1v1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v11);
        Map<String, EDBObjectEntry> data2v1 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 2", data2v1);
        putValue("pre:KeyB", "pre:Value A 1", data2v1);
        EDBObject v12 = new EDBObject("pre:/test/object2", data2v1);
        ci.insert(v12);
        Map<String, EDBObjectEntry> data3v1 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 3", data3v1);
        putValue("pre:KeyB", "pre:Value A 1", data3v1);
        EDBObject v13 = new EDBObject("pre:/test/object3", data3v1);
        ci.insert(v13);

        long time1 = db.commit(ci);

        Map<String, EDBObjectEntry> data1v2 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 1", data1v2);
        putValue("pre:KeyB", "pre:Value A 1", data1v2);
        EDBObject v21 = new EDBObject("pre:/test/object1", data1v2);
        ci = getEDBCommit();
        ci.update(v21);
        Map<String, EDBObjectEntry> data2v2 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 2", data2v2);
        putValue("pre:KeyB", "pre:Value A 1", data2v2);
        EDBObject v22 = new EDBObject("pre:/test/object2", data2v2);
        ci.update(v22);
        Map<String, EDBObjectEntry> data4v1 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 4", data4v1);
        putValue("pre:KeyB", "pre:Value A 1", data4v1);
        EDBObject v23 = new EDBObject("pre:/test/object4", data4v1);
        ci.update(v23);

        long time2 = db.commit(ci);

        Map<String, EDBObjectEntry> data1v3 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 1", data1v3);
        putValue("pre:KeyB", "pre:Value A 1", data1v3);
        EDBObject v31 = new EDBObject("pre:/test/object1", data1v3);
        ci = getEDBCommit();
        ci.update(v31);
        Map<String, EDBObjectEntry> data2v3 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 2a", data2v3);
        putValue("pre:KeyB", "pre:Value A 1", data2v3);
        EDBObject v32 = new EDBObject("pre:/test/object2", data2v3);
        ci.update(v32);
        Map<String, EDBObjectEntry> data4v2 = new HashMap<String, EDBObjectEntry>();
        putValue("pre:KeyA", "pre:Value A 4", data4v2);
        putValue("pre:KeyB", "pre:Value A 1", data4v2);
        EDBObject v33 = new EDBObject("pre:/test/object4", data4v2);
        ci.update(v33);

        long time3 = db.commit(ci);
        List<EDBObject> result = db.query(QueryRequest.query("pre:KeyB", "pre:Value A 1")
            .setTimestamp(time2));

        boolean b1 = false;
        boolean b2 = false;
        boolean b3 = false;

        for (EDBObject e : result) {
            if (e.getString("pre:KeyA").equals("pre:Value A 1")) {
                b1 = true;
            }
            if (e.getString("pre:KeyA").equals("pre:Value A 2")) {
                b2 = true;
            }
            if (e.getString("pre:KeyA").equals("pre:Value A 3")) {
                b3 = true;
            }
        }

        assertThat(b1, is(true));
        assertThat(b2, is(true));
        assertThat(b3, is(true));
        assertThat(time1 > 0, is(true));
        assertThat(time2 > 0, is(true));
        assertThat(time3 > 0, is(true));
    }

    @Test
    public void testQueryWithTimestamp_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("K", "B", data1);
        putValue("Cow", "Milk", data1);
        putValue("Dog", "Food", data1);
        EDBObject v1 = new EDBObject("/test/querynew1", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        putValue("Dog", "Food", data1);
        v1 = new EDBObject("/test/querynew1", data1);
        ci = getEDBCommit();
        ci.update(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        putValue("K", "B", data1);
        putValue("Dog", "Food", data1);
        v1 = new EDBObject("/test/querynew2", data1);
        ci = getEDBCommit();
        ci.insert(v1);
        db.commit(ci);
        List<EDBObject> result = db.query(QueryRequest.query("K", "B"));
        assertThat(result.size(), is(1));
    }

    @Test
    public void testQueryWithTimestampAndEmptyMap_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("K", "B", data1);
        putValue("Cow", "Milk", data1);
        putValue("Dog", "Food", data1);
        EDBObject v1 = new EDBObject("/test/querynew3", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        putValue("Dog", "Food", data1);
        v1 = new EDBObject("/test/querynew3", data1);
        ci = getEDBCommit();
        ci.update(v1);
        db.commit(ci);

        data1 = new HashMap<String, EDBObjectEntry>();
        putValue("K", "B", data1);
        putValue("Dog", "Food", data1);
        v1 = new EDBObject("/test/querynew4", data1);
        ci = getEDBCommit();
        ci.insert(v1);
        db.commit(ci);

        List<EDBObject> result = db.query(QueryRequest.create());
        EDBObject result1 = getEDBObjectOutOfList(result, "/test/querynew3");
        EDBObject result2 = getEDBObjectOutOfList(result, "/test/querynew4");
        assertThat(result.size(), is(2));
        assertThat(result1.containsKey("K"), is(false));
        assertThat(result2.containsKey("Dog"), is(true));
    }

    @Test
    public void testQueryOfLastKnownVersion_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1v1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 1", data1v1);
        putValue("KeyB", "Value A 1", data1v1);
        EDBObject v11 = new EDBObject("/test/object1", data1v1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v11);
        Map<String, EDBObjectEntry> data2v1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 2", data2v1);
        putValue("KeyB", "Value A 1", data2v1);
        EDBObject v12 = new EDBObject("/test/object2", data2v1);
        ci.insert(v12);
        Map<String, EDBObjectEntry> data3v1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 3", data3v1);
        putValue("KeyB", "Value A 1", data3v1);
        EDBObject v13 = new EDBObject("/test/object3", data3v1);
        ci.insert(v13);

        long time1 = db.commit(ci);

        ci = getEDBCommit();
        Map<String, EDBObjectEntry> data1v2 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 1", data1v2);
        putValue("KeyB", "Value A 1", data1v2);
        EDBObject v21 = new EDBObject("/test/object1", data1v2);
        ci.update(v21);
        Map<String, EDBObjectEntry> data2v2 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 2", data2v2);
        putValue("KeyB", "Value A 1", data2v2);
        EDBObject v22 = new EDBObject("/test/object2", data2v2);
        ci.update(v22);

        long time2 = db.commit(ci);

        ci = getEDBCommit();
        Map<String, EDBObjectEntry> data2v3 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 2a", data2v3);
        putValue("KeyB", "Value A 1", data2v3);
        EDBObject v32 = new EDBObject("/test/object2", data2v3);
        ci.update(v32);
        Map<String, EDBObjectEntry> data4v1 = new HashMap<String, EDBObjectEntry>();
        putValue("KeyA", "Value A 4", data4v1);
        putValue("KeyB", "Value A 1", data4v1);
        EDBObject v33 = new EDBObject("/test/object4", data4v1);
        ci.insert(v33);

        long time3 = db.commit(ci);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("KeyB", "Value A 1");
        List<EDBObject> result = db.query(QueryRequest.query("KeyB", "Value A 1").setTimestamp(time3));

        boolean b1 = false;
        boolean b2 = false;
        boolean b3 = false;
        boolean b4 = false;

        for (EDBObject e : result) {
            if (e.getString("KeyA").equals("Value A 1")) {
                b1 = true;
            }
            if (e.getString("KeyA").equals("Value A 2a")) {
                b2 = true;
            }
            if (e.getString("KeyA").equals("Value A 3")) {
                b3 = true;
            }

            if (e.getString("KeyA").equals("Value A 4")) {
                b4 = true;
            }
        }

        assertThat(b1, is(true));
        assertThat(b2, is(true));
        assertThat(b3, is(true));
        assertThat(b4, is(true));
        assertThat(time1 > 0, is(true));
        assertThat(time2 > 0, is(true));
        assertThat(time3 > 0, is(true));
    }

    @Test
    public void testIfQueryingWithLikeWorks_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("bla", "teststring", data1);
        EDBObject v1 = new EDBObject("/test/query/8", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v1);
        db.commit(ci);
        List<EDBObject> result = db.query(QueryRequest.query("bla", "test%").wildcardAware());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getOID(), is("/test/query/8"));
        result = db.query(QueryRequest.query("bla", "test_tring").wildcardAware());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getOID(), is("/test/query/8"));
        result = db.query(QueryRequest.query("bla", "test%").wildcardUnaware());
        assertThat(result.size(), is(0));
    }

    @Test
    public void testIfRetrievingCommitByRevisionWorks_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> entries = new HashMap<String, EDBObjectEntry>();
        entries.put("test", new EDBObjectEntry("test", "test", String.class));
        EDBObject obj = new EDBObject("/test/query/9", entries);
        EDBCommit ci = getEDBCommit();
        ci.insert(obj);
        db.commit(ci);
        EDBCommit test = db.getCommitByRevision(ci.getRevisionNumber().toString());
        assertThat(test, notNullValue());
        assertThat(test, is(ci));
        assertThat(test.getInserts().get(0).getString("test"), is("test"));
    }

    @Test
    public void testIfRetrievingCommitByRevisionWithIntermediateCommitsWorks_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> entries = new HashMap<String, EDBObjectEntry>();
        entries.put("test", new EDBObjectEntry("test", "test", String.class));
        EDBObject obj = new EDBObject("/test/query/10", entries);
        EDBCommit ci = getEDBCommit();
        ci.insert(obj);
        db.commit(ci);
        String revision = ci.getRevisionNumber().toString();
        obj.putEDBObjectEntry("test", "test2", String.class);
        EDBCommit ci2 = getEDBCommit();
        ci2.update(obj);
        db.commit(ci2);
        EDBCommit test = db.getCommitByRevision(revision);
        assertThat(test, notNullValue());
        assertThat(test, is(ci));
        assertThat(test.getInserts().get(0).getString("test"), is("test"));
    }

    @Test(expected = EDBException.class)
    public void testIfRetrievingCommitByInvalidRevisionFails_shouldFail() throws Exception {
        db.getCommitByRevision(UUID.randomUUID().toString());
    }

    @Test
    public void testIfRetrievingCommitRevisionsByRequestGivesCorrectRevisions_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> entries = new HashMap<String, EDBObjectEntry>();
        entries.put("test", new EDBObjectEntry("test", "test", String.class));
        EDBObject obj = new EDBObject("/test/query/11", entries);
        EDBCommit ci = getEDBCommit();
        ci.insert(obj);
        Long timestamp1 = db.commit(ci);
        String revision1 = ci.getRevisionNumber().toString();
        obj.putEDBObjectEntry("test2", "test2", String.class);
        ci = getEDBCommit();
        ci.setComment("this is a comment");
        ci.update(obj);
        Long timestamp2 = db.commit(ci);
        String revision2 = ci.getRevisionNumber().toString();
        CommitQueryRequest request = new CommitQueryRequest();
        request.setCommitter("wrongName");
        assertThat(db.getRevisionsOfMatchingCommits(request).size(), is(0));
        request = new CommitQueryRequest();
        request.setCommitter("wrongContext");
        assertThat(db.getRevisionsOfMatchingCommits(request).size(), is(0));

        request = new CommitQueryRequest();
        request.setStartTimestamp(timestamp1);
        List<CommitMetaInfo> revisions = db.getRevisionsOfMatchingCommits(request);
        assertThat(revisions.size(), is(2));
        assertThat(revisions.get(0).getRevision(), is(revision1));
        assertThat(revisions.get(1).getRevision(), is(revision2));
        request = new CommitQueryRequest();
        request.setStartTimestamp(timestamp1);
        request.setCommitter("testuser");
        revisions = db.getRevisionsOfMatchingCommits(request);
        assertThat(revisions.size(), is(2));
        assertThat(revisions.get(0).getRevision(), is(revision1));
        assertThat(revisions.get(1).getRevision(), is(revision2));
        request = new CommitQueryRequest();
        request.setStartTimestamp(timestamp2);

        revisions = db.getRevisionsOfMatchingCommits(request);
        assertThat(revisions.size(), is(1));
        assertThat(revisions.get(0).getRevision(), is(revision2));
        assertThat(revisions.get(0).getCommitter(), is(COMMITTER));
        assertThat(revisions.get(0).getContext(), is(CONTEXT));
        assertThat(revisions.get(0).getTimestamp(), is(timestamp2));
        assertThat(revisions.get(0).getComment(), is("this is a comment"));
    }
    
    @Test
    public void testIfQueryingWithCaseInsensitivity_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("test", "This is A test", data1);
        EDBObject v1 = new EDBObject("/test/query/12", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v1);
        db.commit(ci);
        List<EDBObject> result = db.query(QueryRequest.query("test", "this is a test").caseInsensitive());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getOID(), is("/test/query/12"));
        result = db.query(QueryRequest.query("test", "this is a test").caseSensitive());
        assertThat(result.size(), is(0));
    }
    
    @Test
    public void testIfQueryingWithCaseSensitivityAndWildcards_shouldWork() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<String, EDBObjectEntry>();
        putValue("test", "This is A new test", data1);
        EDBObject v1 = new EDBObject("/test/query/13", data1);
        EDBCommit ci = getEDBCommit();
        ci.insert(v1);
        db.commit(ci);
        List<EDBObject> result = db.query(QueryRequest.query("test", "this is a % test")
            .caseInsensitive().wildcardAware());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getOID(), is("/test/query/13"));
        result = db.query(QueryRequest.query("test", "this is a % test").caseInsensitive().wildcardUnaware());
        assertThat(result.size(), is(0));
        result = db.query(QueryRequest.query("test", "This is % new test").caseSensitive().wildcardUnaware());
        assertThat(result.size(), is(0));
        result = db.query(QueryRequest.query("test", "This is % new test").caseSensitive().wildcardAware());
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getOID(), is("/test/query/13"));
    }
    
    @Test
    public void testIfLastRevisionNumberOfContextWorks_shouldReturnCorrectRevisions() throws Exception {
        String context1 = "context1";
        String context2 = "context2";
        String context3 = "context3";
        ContextHolder.get().setCurrentContextId(context1);
        EDBObject obj = new EDBObject("/test/context/1");
        EDBCommit commit = getEDBCommit();
        commit.insert(obj);
        db.commit(commit);
        UUID revision1 = commit.getRevisionNumber();
        ContextHolder.get().setCurrentContextId(context2);
        obj = new EDBObject("/test/context/2");
        commit = getEDBCommit();
        commit.insert(obj);
        db.commit(commit);
        UUID revision2 = commit.getRevisionNumber();
        ContextHolder.get().setCurrentContextId(context3);
        obj = new EDBObject("/test/context/3");
        commit = getEDBCommit();
        commit.insert(obj);
        db.commit(commit);
        UUID revision3 = commit.getRevisionNumber();
        ContextHolder.get().setCurrentContextId(CONTEXT);
        obj = new EDBObject("/test/context/4");
        commit = getEDBCommit();
        commit.insert(obj);
        db.commit(commit);
        
        assertThat(db.getLastRevisionNumberOfContext(context1), is(revision1));
        assertThat(db.getLastRevisionNumberOfContext(context2), is(revision2));
        assertThat(db.getLastRevisionNumberOfContext(context3), is(revision3));
        assertThat(db.getLastRevisionNumberOfContext("notExistingContext"), nullValue());
    }
    
    @Test
    public void testIfOrRequestsAreWorking_shouldReturnCorrectObjects() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<>();
        putValue("Cow", "Milk", data1);
        putValue("Cheese", "Cheddar", data1);
        EDBObject v1 = new EDBObject("/test/query/14/1", data1);
        Map<String, EDBObjectEntry> data2 = new HashMap<>();
        putValue("Animal", "Dog", data2);
        putValue("Cow", "Milk", data2);
        EDBObject v2 = new EDBObject("/test/query/14/2", data2);
        Map<String, EDBObjectEntry> data3 = new HashMap<>();
        putValue("House", "Garden", data3);
        EDBObject v3 = new EDBObject("/test/query/14/3", data3);
        Map<String, EDBObjectEntry> data4 = new HashMap<>();
        putValue("Cheese", "Cheddar", data4);
        putValue("Animal", "Dog", data4);
        EDBObject v4 = new EDBObject("/test/query/14/4", data4);
        EDBCommit commit = getEDBCommit();
        commit.insert(v1);
        commit.insert(v2);
        commit.insert(v3);
        commit.insert(v4);
        db.commit(commit);
        QueryRequest request = QueryRequest.create().orJoined();
        request.addParameter("Cow", "Milk");
        assertThat(db.query(request).size(), is(2));
        request.addParameter("Animal", "Dog");
        assertThat(db.query(request).size(), is(3));
        request.removeParameter("Cow");
        assertThat(db.query(request).size(), is(2));
        request.addParameter("Cow", "Milk").addParameter("House", "Garden");
        assertThat(db.query(request).size(), is(4));
    }
    
    @Test
    public void testIfContextSpecificQueriesWork_shouldReturnCorrectObjects() throws Exception {
        Map<String, EDBObjectEntry> data1 = new HashMap<>();
        putValue("Cow", "Milk", data1);
        putValue("Cheese", "Cheddar", data1);
        EDBObject v1 = new EDBObject("/test/query/15/1", data1);
        Map<String, EDBObjectEntry> data2 = new HashMap<>();
        putValue("Animal", "Dog", data2);
        putValue("Cow", "Milk", data2);
        EDBObject v2 = new EDBObject("/test/query/15/2", data2);
        EDBCommit commit = getEDBCommit();
        commit.insert(v1);
        commit.insert(v2);
        db.commit(commit);
        
        Map<String, EDBObjectEntry> data3 = new HashMap<>();
        putValue("House", "Garden", data3);
        EDBObject v3 = new EDBObject("/test2/query/15/3", data3);
        Map<String, EDBObjectEntry> data4 = new HashMap<>();
        putValue("Cheese", "Cheddar", data4);
        putValue("Animal", "Dog", data4);
        EDBObject v4 = new EDBObject("/test2/query/15/4", data4);
        commit = getEDBCommit();
        commit.insert(v3);
        commit.insert(v4);
        db.commit(commit);
        QueryRequest request = QueryRequest.create().orJoined();
        request.addParameter("Animal", "Dog");
        assertThat(db.query(request).size(), is(2));
        request.setContextId("/test");
        List<EDBObject> result = db.query(request);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getOID(), is("/test/query/15/2"));
        
        request.setContextId("/test2");
        result = db.query(request);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getOID(), is("/test2/query/15/4"));
    }
    
    
}
