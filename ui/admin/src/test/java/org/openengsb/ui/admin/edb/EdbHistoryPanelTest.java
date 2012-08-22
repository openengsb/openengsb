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
package org.openengsb.ui.admin.edb;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.list.ListView;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBLogEntry;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.ui.admin.AbstractUITest;

public class EdbHistoryPanelTest extends AbstractUITest {
    private EngineeringDatabaseService edbService;

    private static final long TIMESTAMP = 1340914655000L;

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        edbService = mock(EngineeringDatabaseService.class);
        EDBCommit commit = mock(EDBCommit.class);
        when(commit.getCommitter()).thenReturn("adams");
        when(commit.getTimestamp()).thenReturn(TIMESTAMP);
        final EDBLogEntry logEntry = mock(EDBLogEntry.class);
        when(logEntry.getCommit()).thenReturn(commit);
        when(edbService.getLog(eq("null/null/42"), eq(0L), any(Long.class))).thenReturn(Arrays.asList(logEntry));
        context.putBean("edbService", edbService);
    }

    @Test
    public void testInitPanel_shouldShowHistory() throws Exception {
        tester.startPage(new EdbHistoryPanel("42"));
        tester.debugComponentTrees();
        ListView<?> list = (ListView<?>) tester.getComponentFromLastRenderedPage("history");
        Component line = list.get(0);
        assertThat(line.get("time").getDefaultModelObjectAsString(), containsString("Jun 28"));
        assertThat(line.get("committer").getDefaultModelObjectAsString(), is("adams"));
    }
}
