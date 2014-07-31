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
package org.openengsb.core.api.model;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class QueryRequestTest {

    /**
     * Shared timestamp
     */
    private long timestamp;

    @Before
    public void setUp() throws Exception {
        timestamp = System.currentTimeMillis();
    }

    @Test
    public void equals_onEqualObjcets_shouldReturnTrue() throws Exception {
        QueryRequest a = QueryRequest.create()
            .setTimestamp(timestamp)
            .addParameter("foo", "bar")
            .addParameter("answer", 42)
            .setContextId("someContext");

        QueryRequest b = QueryRequest.create()
            .setTimestamp(timestamp)
            .addParameter("foo", "bar")
            .setContextId("someContext")
            .addParameter("answer", 42);

        assertTrue(a.equals(b));
    }

    @Test
    public void equals_onNonEqualObjcets_shouldReturnFalse() throws Exception {
        QueryRequest a = QueryRequest.create()
            .setTimestamp(timestamp)
            .addParameter("foo", "bar")
            .addParameter("answer", 42)
            .setContextId("someContext")
            .wildcardAware();

        QueryRequest b = QueryRequest.create()
            .setTimestamp(timestamp)
            .addParameter("foo", "bar")
            .setContextId("someContext")
            .addParameter("answer", 42)
            .wildcardUnaware();

        assertFalse(a.equals(b));
    }

    @Test
    public void hashCode_onEqualObjcets_shouldReturnSameHashCode() throws Exception {
        QueryRequest a = QueryRequest.create()
            .setTimestamp(timestamp)
            .addParameter("foo", "bar")
            .addParameter("answer", 42)
            .setContextId("someContext");

        QueryRequest b = QueryRequest.create()
            .setTimestamp(timestamp)
            .addParameter("foo", "bar")
            .setContextId("someContext")
            .addParameter("answer", 42);

        assertThat(a.hashCode(), is(b.hashCode()));
    }

    @Test
    public void hashCode_onNonEqualObjcets_shouldReturnDifferentHashCodes() throws Exception {
        QueryRequest a = QueryRequest.create()
            .setTimestamp(timestamp)
            .addParameter("foo", "bar")
            .addParameter("answer", 42)

            .setContextId("someContext")
            .wildcardAware();

        QueryRequest b = QueryRequest.create()
            .setTimestamp(timestamp)
            .addParameter("foo", "bar")
            .setContextId("someContext")
            .addParameter("answer", 42)
            .wildcardUnaware();

        assertThat(a.hashCode(), is(not(b.hashCode())));
    }

}
