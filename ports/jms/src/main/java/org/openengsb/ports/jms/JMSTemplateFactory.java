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

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

/**
 * Internal factory to create jmsTemplates and message listeners.
 */
public interface JMSTemplateFactory {

    /**
     * Creates a JMS Template taking the queue/topic and host settings from the destination param
     */
    JmsTemplate createJMSTemplate(DestinationUrl destination);

    /**
     * Creates a simple message listener container without any furhter configurations. Those have to be done by the
     * client.
     */
    SimpleMessageListenerContainer createMessageListenerContainer();
}
