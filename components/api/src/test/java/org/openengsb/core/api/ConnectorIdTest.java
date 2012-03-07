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

package org.openengsb.core.api;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.openengsb.core.api.model.ConnectorDefinition;

public class ConnectorIdTest {

    @Test
    public void testParseConnectorId_shouldReturnCorrectId() throws Exception {
        ConnectorDefinition id = new ConnectorDefinition("mydomain", "myconnector", "xxx");
        String text = id.toString();
        ConnectorDefinition id2 = ConnectorDefinition.fromFullId(text);
        assertThat(id2, is(id));
    }

    @Test
    public void testParseIdWithSpecial_shouldReturnCorrectId() throws Exception {
        ConnectorDefinition id = new ConnectorDefinition("mydomain", "myconnector", "x+x-x");
        String text = id.toString();
        ConnectorDefinition id2 = ConnectorDefinition.fromFullId(text);
        assertThat(id2, is(id));
    }

    @Test
    public void testConnectorIdEqualsConnectorId_shouldBeEqual() throws Exception {
        ConnectorDefinition id1 = ConnectorDefinition.generate("test", "testc");
        ConnectorDefinition id2 = new ConnectorDefinition("test", "testc", id1.getInstanceId());
        assertThat(id1.equals(id2), is(true));
    }

    @Test
    public void testConnectorIdEqualsDifferentId_shouldNotBeEqual() throws Exception {
        ConnectorDefinition id1 = ConnectorDefinition.generate("test", "testc");
        ConnectorDefinition id2 = new ConnectorDefinition("test", "testc", "not-equal");
        assertThat(id1.equals(id2), is(false));
    }

    @Test
    public void testConnectorIdEqualsMetadata_shouldBeEqual() throws Exception {
        ConnectorDefinition id1 = ConnectorDefinition.generate("test", "testc");
        Map<String, String> metaData = id1.toMetaData();
        assertThat(id1.equals(metaData), is(true));
    }

    @Test
    public void testConnectorIdEqualsOtherMetadata_shouldNotBeEqual() throws Exception {
        ConnectorDefinition id1 = ConnectorDefinition.generate("test", "testc");
        Map<String, String> metaData = id1.toMetaData();
        metaData.put(Constants.CONNECTOR_KEY, "something-different");
        assertThat(id1.equals(metaData), is(false));
    }

    @Test
    public void testConnectorIdEqualsFullIdString_shouldBeEqual() throws Exception {
        ConnectorDefinition id1 = ConnectorDefinition.generate("test", "testc");
        String fullId = id1.toFullID();
        assertThat(id1.equals(fullId), is(true));
    }

    @Test
    public void testConnectorIdEqualsOtherString_shouldNotBeEqual() throws Exception {
        ConnectorDefinition id1 = ConnectorDefinition.generate("test", "testc");
        String otherId = id1.toFullID() + " ";
        assertThat(id1.equals(otherId), is(false));
    }
}
