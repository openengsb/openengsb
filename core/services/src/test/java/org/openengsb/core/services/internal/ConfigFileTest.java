package org.openengsb.core.services.internal;

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
import org.openengsb.core.services.internal.deployer.connector.ConnectorFile;
import org.openengsb.core.services.internal.deployer.connector.ConnectorFile.ChangeSet;

public class ConfigFileTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private File connectorFile;

    @Before
    public void setUp() throws Exception {
        connectorFile = tempFolder.newFile("d+c+my.connector");
    }

    @Test
    public void testInitialize_shouldReturnCorrectConfiguration() throws Exception {
        FileUtils.writeLines(connectorFile, Arrays.asList("property.foo=bar", "attribute.test=42"));
        ConnectorFile fileObject = new ConnectorFile(connectorFile);
        assertThat(fileObject.getConnectorId().equals("d+c+my"), is(true));
        assertThat((String) fileObject.getProperties().get("foo"), is("bar"));
        assertThat(fileObject.getAttributes().get("test"), is("42"));
    }

    @Test
    public void testInitialzeWithArray_shouldReturnArrayProperty() throws Exception {
        FileUtils.writeLines(connectorFile, Arrays.asList("property.foo=bar,42"));
        ConnectorFile fileObject = new ConnectorFile(connectorFile);
        String[] values = (String[]) fileObject.getProperties().get("foo");
        assertThat(Arrays.asList(values), hasItems("bar", "42"));
    }

    @Test
    public void testUpdateAddNewProperty_shouldShowChangedValueInResult() throws Exception {
        FileUtils.writeLines(connectorFile, Arrays.asList("property.foo=bar,42"));
        ConnectorFile fileObject = new ConnectorFile(connectorFile);
        FileUtils.writeLines(connectorFile, Arrays.asList("property.foo=bar,42", "property.test=xxx"));
        ChangeSet update = fileObject.update(connectorFile);
        update.getChangedProperties().entriesOnlyOnRight().containsKey("test");
    }

    @Test
    public void testChangeProperty_shouldShowChangedValueInResult() throws Exception {
        FileUtils.writeLines(connectorFile, Arrays.asList("property.foo=bar"));
        ConnectorFile fileObject = new ConnectorFile(connectorFile);
        FileUtils.writeLines(connectorFile, Arrays.asList("property.foo=bar,42"));
        ChangeSet update = fileObject.update(connectorFile);
        update.getChangedProperties().entriesOnlyOnRight().containsKey("test");
    }

}
