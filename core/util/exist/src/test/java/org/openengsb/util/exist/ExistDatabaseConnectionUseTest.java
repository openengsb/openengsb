/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.util.exist;

import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests only trying to analyse the behaviour of exist and the little
 * implementation we have to do. These tests are not meant to run each time, but
 * only to learn the interface or if it gets updated.
 */
public class ExistDatabaseConnectionUseTest {

    private final static String DATABASE_CONNECTION_URI = "xmldb:exist://localhost:8093/exist/xmlrpc";

    @Test
    @Ignore
    public void testStoringContentToExist() throws Exception {
        DatabaseConnection connection = createExistDatabaseConnection();
        String reusableValue = UUID.randomUUID().toString();
        connection.storeContentNodeToDatabase("/db/" + reusableValue + "/" + reusableValue + "/" + reusableValue,
                reusableValue, "<reusableValue/>");
    }

    @Test(expected = DatabaseException.class)
    @Ignore
    public void testStoringMoreComplexContentToExistWithStructuringError() throws Exception {
        DatabaseConnection connection = createExistDatabaseConnection();
        String content = "<logEntry><prop name=\"contextId\">42</prop><prop name=\"correlationId\">"
                + "0bbd7c36-b636-4e30-87da-d1f53703d107</prop><prop name=\"workflowId\">ci</prop>"
                + "<prop name=\"workflowInstanceId\">3672e1b0-2abb-417b-a6ed-88f146099a01</prop><content>"
                + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<list xmlns=\"http://org.openengsb/util/serialization\" name=\"event\" format=\"\" domainConcept=\"\">"
                + "<text name=\"event\" format=\"\" domainConcept=\"\">org.openengsb.drools.events.BuildStartEvent</text>"
                + "<list name=\"superclasses\" format=\"\" domainConcept=\"\">"
                + "<text name=\"superclass\" format=\"\" domainConcept=\"\">org.openengsb.core.model.Event</text>"
                + "<text name=\"superclass\" format=\"\" domainConcept=\"\">java.lang.Object</text></list>"
                + "<text name=\"name\" format=\"\" domainConcept=\"\">buildStartedEvent</text>"
                + "<text name=\"domain\" format=\"\" domainConcept=\"\">build</text>"
                + "<text name=\"toolConnector\" format=\"\" domainConcept=\"\">maven-build</text>"
                + "<list name=\"element\" format=\"\" domainConcept=\"\">"
                + "<text name=\"name\" format=\"\" domainConcept=\"\">parameters</text><text name=\"type\" format=\"\" domainConcept=\"\">java.lang.String</text>"
                + "<text name=\"value\" format=\"\" domainConcept=\"\">MavenParameters [baseDir=data/openengsb/testProject, executionRequestProperties={}, goals=[package]]</text>"
                + "</list></list></content></logEntry>";
        connection.storeContentNodeToDatabase("/db/TEST/logging/unspecified", UUID.randomUUID().toString(), content);
    }

    @Test
    @Ignore
    public void testStoringMoreComplexContentToExist() throws Exception {
        DatabaseConnection connection = createExistDatabaseConnection();
        String content = "<logEntry><prop name=\"contextId\">42</prop><prop name=\"correlationId\">"
                + "0bbd7c36-b636-4e30-87da-d1f53703d107</prop><prop name=\"workflowId\">ci</prop>"
                + "<prop name=\"workflowInstanceId\">3672e1b0-2abb-417b-a6ed-88f146099a01</prop><content>"
                + "<list xmlns=\"http://org.openengsb/util/serialization\" name=\"event\" format=\"\" domainConcept=\"\">"
                + "<text name=\"event\" format=\"\" domainConcept=\"\">org.openengsb.drools.events.BuildStartEvent</text>"
                + "<list name=\"superclasses\" format=\"\" domainConcept=\"\">"
                + "<text name=\"superclass\" format=\"\" domainConcept=\"\">org.openengsb.core.model.Event</text>"
                + "<text name=\"superclass\" format=\"\" domainConcept=\"\">java.lang.Object</text></list>"
                + "<text name=\"name\" format=\"\" domainConcept=\"\">buildStartedEvent</text>"
                + "<text name=\"domain\" format=\"\" domainConcept=\"\">build</text>"
                + "<text name=\"toolConnector\" format=\"\" domainConcept=\"\">maven-build</text>"
                + "<list name=\"element\" format=\"\" domainConcept=\"\">"
                + "<text name=\"name\" format=\"\" domainConcept=\"\">parameters</text><text name=\"type\" format=\"\" domainConcept=\"\">java.lang.String</text>"
                + "<text name=\"value\" format=\"\" domainConcept=\"\">MavenParameters [baseDir=data/openengsb/testProject, executionRequestProperties={}, goals=[package]]</text>"
                + "</list></list></content></logEntry>";
        connection.storeContentNodeToDatabase("/db/TEST/logging/unspecified", UUID.randomUUID().toString(), content);
    }

    private ExistDatabaseConnection createExistDatabaseConnection() {
        ExistDatabaseConnection connection = new ExistDatabaseConnection();
        connection.setConnectionUri(DATABASE_CONNECTION_URI);
        return connection;
    }

}
