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

import java.io.IOException;
import java.util.UUID;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.xml.sax.SAXException;

public class ExistLoggingHelper implements LoggingHelper {

    private static final String XML_RROCEEDING_WITH_REGEX = "\\<\\?.+\\?\\>";

    private DatabaseConnection databaseConnection;

    public void setDatabaseConnection(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    @Override
    public void log(NormalizedMessage jbiMessage) {
        String nodeStructure = createNodePath();
        String nodeName = UUID.randomUUID().toString();
        StringBuilder logMessage = createLogMessage(jbiMessage);
        this.databaseConnection.storeContentNodeToDatabase(nodeStructure, nodeName, logMessage.toString());
    }

    /**
     * This could be different for different type of messages. This method may
     * be completely used by another object abstracting this functionality.
     */
    private String createNodePath() {
        return "/db/openengsb/logging/events";
    }

    private StringBuilder createLogMessage(NormalizedMessage jbiMessage) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("<logEntry>");
        for (Object propertyName : jbiMessage.getPropertyNames()) {
            logMessage.append("<prop name=\"").append((String) propertyName).append("\">");
            logMessage.append(jbiMessage.getProperty((String) propertyName));
            logMessage.append("</prop>");
        }
        logMessage.append("<content>");
        logMessage.append(transformMessageContentToCorrectedString(jbiMessage));
        logMessage.append("</content>");
        logMessage.append("</logEntry>");
        return logMessage;
    }

    private String transformMessageContentToCorrectedString(NormalizedMessage jbiMessage) {
        String plainTransformedMessageContent = transformMessageContentDirectlyToString(jbiMessage);
        String cleanedupMessageContent = cleanupTransformedMessageContent(plainTransformedMessageContent);
        return cleanedupMessageContent;
    }

    private String transformMessageContentDirectlyToString(NormalizedMessage jbiMessage) {
        try {
            return new SourceTransformer().contentToString(jbiMessage);
        } catch (MessagingException e) {
            throw new LoggingException();
        } catch (TransformerException e) {
            throw new LoggingException();
        } catch (ParserConfigurationException e) {
            throw new LoggingException();
        } catch (IOException e) {
            throw new LoggingException();
        } catch (SAXException e) {
            throw new LoggingException();
        }
    }

    private String cleanupTransformedMessageContent(String messageContent) {
        return messageContent.replaceAll(XML_RROCEEDING_WITH_REGEX, "");
    }

}
