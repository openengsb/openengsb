/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.util.exist;

import java.util.Arrays;
import java.util.HashSet;

import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.junit.Test;
import org.mockito.Mockito;

public class ExistLoggingHelperTest {

    private static final String PROPERTY_KEY = "prop";
    private static final String PROPERTY_VALUE = "propValue";
    private static final String EVENT_LOG_STRUCTURE = "/db/openengsb/logging/events";
    private static final String UNSPECIFIED_LOG_CONTENT = "<some><content/></some>";
    private static final String PROBLEMATIC_LOG_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    @Test
    public void testLog() throws Exception {
        NormalizedMessage messageToLog = Mockito.mock(NormalizedMessage.class);
        setPropertyToNormalizedMessageMock(PROPERTY_KEY, PROPERTY_VALUE, messageToLog);
        setStringContentToNormalizedMessage(UNSPECIFIED_LOG_CONTENT, messageToLog);
        DatabaseConnection databaseConnection = Mockito.mock(DatabaseConnection.class);
        LoggingHelper loggingHelper = createExistLoggingHelper(databaseConnection);

        loggingHelper.log(messageToLog);

        String modifiedUnspecifiedContent = "<logEntry><prop name=\"prop\">propValue</prop><content>"
                + UNSPECIFIED_LOG_CONTENT + "</content></logEntry>";
        Mockito.verify(databaseConnection, Mockito.times(1)).storeContentNodeToDatabase(
                Mockito.eq(EVENT_LOG_STRUCTURE), Mockito.anyString(), Mockito.eq(modifiedUnspecifiedContent));
    }

    @Test
    public void testProblematicLog() throws Exception {
        NormalizedMessage messageToLog = Mockito.mock(NormalizedMessage.class);
        setPropertyToNormalizedMessageMock(PROPERTY_KEY, PROPERTY_VALUE, messageToLog);
        setStringContentToNormalizedMessage(PROBLEMATIC_LOG_CONTENT + UNSPECIFIED_LOG_CONTENT, messageToLog);
        DatabaseConnection databaseConnection = Mockito.mock(DatabaseConnection.class);
        LoggingHelper loggingHelper = createExistLoggingHelper(databaseConnection);

        loggingHelper.log(messageToLog);

        String modifiedUnspecifiedContent = "<logEntry><prop name=\"prop\">propValue</prop><content>"
                + UNSPECIFIED_LOG_CONTENT + "</content></logEntry>";
        Mockito.verify(databaseConnection, Mockito.times(1)).storeContentNodeToDatabase(
                Mockito.eq(EVENT_LOG_STRUCTURE), Mockito.anyString(), Mockito.eq(modifiedUnspecifiedContent));
    }

    private void setPropertyToNormalizedMessageMock(String propertyKey, String propertyValue,
            NormalizedMessage messageToLog) {
        Mockito.when(messageToLog.getPropertyNames()).thenReturn(
                new HashSet<String>(Arrays.asList(new String[] { propertyKey })));
        Mockito.when(messageToLog.getProperty(propertyKey)).thenReturn(propertyValue);
    }

    private void setStringContentToNormalizedMessage(String content, NormalizedMessage messageToLog) {
        Mockito.when(messageToLog.getContent()).thenReturn(new StringSource(content));
    }

    private LoggingHelper createExistLoggingHelper(DatabaseConnection databaseConnection) {
        ExistLoggingHelper loggingHelper = new ExistLoggingHelper();
        loggingHelper.setDatabaseConnection(databaseConnection);
        return loggingHelper;
    }
}
