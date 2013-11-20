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

package org.openengsb.core.ekb.persistence.query.edb;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.QueryParser;
import org.openengsb.core.ekb.persistence.query.edb.internal.DefaultQueryParser;

public class DefaultQueryParserTest {
    private static QueryParser parser;

    @BeforeClass
    public static void setup() {
        parser = new DefaultQueryParser();
    }

    @Test
    public void testRegexCheckForAndJoinedQueries_shouldWork() throws Exception {
        assertWorking("combined query with two conditions don't work", "a:\"b\" and b:\"c\"");
        assertWorking("combined query with three conditions don't work", "a:\"b\" and b:\"c\" and c:\"d\"");
    }

    @Test
    public void testRegexCheckForOrJoinedQueries_shouldWork() throws Exception {
        assertWorking("combined query with two conditions don't work", "a:\"b\" or b:\"c\"");
        assertWorking("combined query with three conditions don't work", "a:\"b\" or b:\"c\" or c:\"d\"");
        assertWorking("combined query with two equal keys don't work", "a:\"b\" or a:\"c\"");
    }

    @Test
    public void testInvalidQueries_shouldReturnFalse() throws Exception {
        assertFailing("query with 'and' and no other condition works", "a:\"b\" and ");
        assertFailing("mixing of 'and' and 'or' works", "a:\"b\" and c:\"d\" or e:\"f\"");
        assertFailing("query with unknown binding word works", "a:\"b\" test c:\"d\"");
    }

    @Test
    public void testSpecialQueries_shouldReturnTrue() throws Exception {
        assertWorking("empty query work", "");
        assertWorking("query with one condition don't work", "a:\"b\"");
    }

    @Test
    public void testAndQueryRequestObjectConstruction_shouldBuildCorrectObjects() throws Exception {
        QueryRequest request = parser.parseQueryString("a:\"b\" and c:\"d\"");
        assertThat(request.isAndJoined(), is(true));
        assertThat(request.getParameter("a").contains("b"), is(true));
        assertThat(request.getParameter("c").contains("d"), is(true));
        request = parser.parseQueryString("a:\"b\" and c:\"d\" and e:\"f\"");
        assertThat(request.isAndJoined(), is(true));
        assertThat(request.getParameter("a").contains("b"), is(true));
        assertThat(request.getParameter("c").contains("d"), is(true));
        assertThat(request.getParameter("e").contains("f"), is(true));
    }

    @Test
    public void testOrQueryRequestObjectConstruction_shouldBuildCorrectObjects() throws Exception {
        QueryRequest request = parser.parseQueryString("a:\"b\" or c:\"d\"");
        assertThat(request.isAndJoined(), is(false));
        assertThat(request.getParameter("a").contains("b"), is(true));
        assertThat(request.getParameter("c").contains("d"), is(true));
        request = parser.parseQueryString("a:\"b\" or c:\"d\" or e:\"f\"");
        assertThat(request.isAndJoined(), is(false));
        assertThat(request.getParameter("a").contains("b"), is(true));
        assertThat(request.getParameter("c").contains("d"), is(true));
        assertThat(request.getParameter("e").contains("f"), is(true));
    }

    @Test
    public void testSpecialQueryRequestObjectConstruction_shouldBuildCorrectObjects() throws Exception {
        QueryRequest request = parser.parseQueryString("");
        assertThat(request.getParameters().size(), is(0));
        request = parser.parseQueryString("a:\"b\"");
        assertThat(request.getParameter("a").contains("b"), is(true));
    }

    @Test(expected = EKBException.class)
    public void testInvalidQueryRequestObjectConstruction_shouldThrowException() throws Exception {
        parser.parseQueryString("a:\"b\" test c:\"d\"");
    }

    private void assertWorking(String failureMessage, String query) {
        assertThat(failureMessage, parser.isParsingPossible(query), is(true));
    }

    private void assertFailing(String failureMessage, String query) {
        assertThat(failureMessage, parser.isParsingPossible(query), is(false));
    }
}
