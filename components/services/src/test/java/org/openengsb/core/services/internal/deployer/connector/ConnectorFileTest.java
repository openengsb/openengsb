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

package org.openengsb.core.services.internal.deployer.connector;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.services.internal.deployer.connector.ConnectorFile.ChangeSet;

public class ConnectorFileTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private File connectorFile;

    @Before
    public void setUp() throws Exception {
        connectorFile = tempFolder.newFile("d+c+my.connector");
    }

    @Test
    public void testInitialize_shouldReturnCorrectConfiguration() throws Exception {
        FileUtils.writeLines(connectorFile, Arrays.asList(
            "domainType=d",
            "connectorType=c",
            "property.foo=bar",
            "attribute.test=42"));
        ConnectorFile fileObject = new ConnectorFile(connectorFile);
        assertThat(fileObject.getName().equals("d+c+my"), is(true));
        assertThat((String) fileObject.getProperties().get("foo"), is("bar"));
        assertThat(fileObject.getAttributes().get("test"), is("42"));
    }

    @Test
    public void testInitialzeWithArray_shouldReturnArrayProperty() throws Exception {
        FileUtils.writeLines(connectorFile, Arrays.asList(
            "domainType=d",
            "connectorType=c",
            "property.foo=bar,42"));
        ConnectorFile fileObject = new ConnectorFile(connectorFile);
        String[] values = (String[]) fileObject.getProperties().get("foo");
        assertThat(Arrays.asList(values), hasItems("bar", "42"));
    }

    @Test
    public void testInitialzeWithUntrimedProperties_shouldTrimProperties() throws Exception {
        FileUtils.writeLines(connectorFile, Arrays.asList(
            "domainType=d   ",
            "connectorType=c   ",
            "property.foo=   bar  ",
            "attribute.test=  42  "));
        ConnectorFile fileObject = new ConnectorFile(connectorFile);
        assertThat(fileObject.getName().equals("d+c+my"), is(true));
        assertThat((String) fileObject.getProperties().get("foo"), is("bar"));
        assertThat(fileObject.getAttributes().get("test"), is("42"));
    }

    @Test
    public void testUpdateAddNewProperty_shouldShowChangedValueInResult() throws Exception {
        FileUtils.writeLines(connectorFile, Arrays.asList("domainType=d",
            "connectorType=c", "property.foo=bar,42"));
        ConnectorFile fileObject = new ConnectorFile(connectorFile);
        FileUtils.writeLines(connectorFile, Arrays.asList("property.foo=bar,42", "property.test=xxx"));
        ChangeSet update = fileObject.getChanges(connectorFile);
        update.getChangedProperties().entriesOnlyOnRight().containsKey("test");
    }

    @Test
    public void testChangeProperty_shouldShowChangedValueInResult() throws Exception {
        FileUtils.writeLines(connectorFile, Arrays.asList("domainType=d",
            "connectorType=c", "property.foo=bar"));
        ConnectorFile fileObject = new ConnectorFile(connectorFile);
        FileUtils.writeLines(connectorFile, Arrays.asList("domainType=d",
            "connectorType=c", "property.foo=bar,42"));
        ChangeSet update = fileObject.getChanges(connectorFile);
        update.getChangedProperties().entriesOnlyOnRight().containsKey("test");
    }

}
