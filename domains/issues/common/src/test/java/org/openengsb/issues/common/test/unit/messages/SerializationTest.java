/**

Copyright 2009 OpenEngSB Division, Vienna University of Technology

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */

package org.openengsb.issues.common.test.unit.messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.issues.common.messages.CreateIssueMessage;
import org.openengsb.issues.common.messages.CreateIssueResponseMessage;
import org.openengsb.issues.common.messages.CreateIssueStatus;
import org.openengsb.issues.common.model.Issue;
import org.openengsb.issues.common.model.IssuePriority;
import org.openengsb.issues.common.model.IssueSeverity;
import org.openengsb.issues.common.model.IssueType;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.SerializationException;
import org.openengsb.util.serialization.Serializer;

import static org.junit.Assert.*;

public class SerializationTest {

    private Serializer serializer;

    private String summary = "Test Summary";
    private String description = "Test Description";
    private String reporter = "Test Reporter";
    private String owner = "Test Owner";
    private IssueType type = IssueType.BUG;
    private IssuePriority priority = IssuePriority.HIGH;
    private IssueSeverity severity = IssueSeverity.BLOCK;
    private String affectedVersion = "1.0";

    private String createdIssueId = "Test Issue ID 1";
    private CreateIssueStatus status = CreateIssueStatus.SUCCESS;
    private String statusMessage = "Test Status Message";

    @Before
    public void setup() {
        serializer = new JibxXmlSerializer();
    }

    @Test
    public void JibxSerializationOfCreateIssueMessageShouldSucceedWithValidInput() throws URISyntaxException,
            SerializationException, IOException {
        File validFile = new File(ClassLoader.getSystemResource("valid-createissuemessage.xml").toURI());

        CreateIssueMessage msg = new CreateIssueMessage(new Issue(summary, description, reporter, owner, type,
                priority, severity, affectedVersion));

        StringWriter sw = new StringWriter();

        serializer = new JibxXmlSerializer();
        serializer.serialize(msg, sw);

        assertEquals(getWhitespaceAdjustedTextFromFile(validFile), sw.toString());
    }

    @Test
    public void JibxDeserializationOfCreateIssueMessageShouldSucceedWithValidInput() throws URISyntaxException,
            FileNotFoundException, SerializationException {
        File validFile = new File(ClassLoader.getSystemResource("valid-createissuemessage.xml").toURI());

        serializer = new JibxXmlSerializer();

        CreateIssueMessage msg = serializer.deserialize(CreateIssueMessage.class, new FileReader(validFile));

        assertNotNull(msg);
        assertNotNull(msg.getIssue());
        assertEquals(summary, msg.getIssue().getSummary());
        assertEquals(description, msg.getIssue().getDescription());
        assertEquals(reporter, msg.getIssue().getReporter());
        assertEquals(owner, msg.getIssue().getOwner());
        assertEquals(type, msg.getIssue().getType());
        assertEquals(priority, msg.getIssue().getPriority());
    }

    @Test
    public void JibxSerializationOfCreateIssueResponseMessageShouldSucceedWithValidInput() throws URISyntaxException,
            SerializationException, IOException {
        File validFile = new File(ClassLoader.getSystemResource("valid-createissueresponsemessage.xml").toURI());

        CreateIssueResponseMessage msg = new CreateIssueResponseMessage(createdIssueId, status, statusMessage);

        StringWriter sw = new StringWriter();

        serializer = new JibxXmlSerializer();
        serializer.serialize(msg, sw);

        assertEquals(getWhitespaceAdjustedTextFromFile(validFile), sw.toString());
    }

    @Test
    public void JibxDeserializationOfCreateIssueResponseMessageShouldSucceedWithValidInput() throws URISyntaxException,
            FileNotFoundException, SerializationException {
        File validFile = new File(ClassLoader.getSystemResource("valid-createissueresponsemessage.xml").toURI());

        serializer = new JibxXmlSerializer();

        CreateIssueResponseMessage msg = serializer.deserialize(CreateIssueResponseMessage.class, new FileReader(
                validFile));

        assertNotNull(msg);
        assertEquals(createdIssueId, msg.getCreatedIssueId());
        assertEquals(status, msg.getStatus());
        assertEquals(statusMessage, msg.getStatusMessage());
    }

    private String getWhitespaceAdjustedTextFromFile(File file) throws IOException {
        return FileUtils.readFileToString(file).replaceAll("\n", "").replaceAll(">\\s*<", "><").replaceAll("<!--.*-->",
                "");
    }
}
