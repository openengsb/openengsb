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

package org.openengsb.ports.jms;

import com.google.common.base.Preconditions;

/**
 * Wrapper to create a host/queue|topic connection based on an destination string.
 */
public final class DestinationUrl {

    private String host;
    private String jmsDestination;

    /**
     * Creates an instance of an connection URL based on an destination string. In case that the destination string does
     * not match the form HOST?QUEUE||TOPIC an IllegalArgumentException is thrown.
     */
    public static DestinationUrl createDestinationUrl(String destination) {
        String[] split = splitDestination(destination);
        String host = split[0].trim();
        String jmsDestination = split[1].trim();
        return new DestinationUrl(host, jmsDestination);
    }

    private static String[] splitDestination(String destination) {
        String[] split = destination.split("\\?");
        Preconditions.checkArgument(split.length == 2, "The destination have to be of the form HOST?QUEUE|TOPIC");
        Preconditions.checkArgument(!split[0].isEmpty(), "A host have to be configured");
        Preconditions.checkArgument(!split[1].isEmpty(), "A queue or topic have to be specified");
        return split;
    }

    private DestinationUrl(String host, String jmsDestination) {
        this.host = host;
        this.jmsDestination = jmsDestination;
    }

    public String getHost() {
        return host;
    }

    public String getJmsDestination() {
        return jmsDestination;
    }

}
