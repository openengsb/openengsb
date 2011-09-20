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

package org.openengsb.core.api.security.model;

import java.io.Serializable;

/**
 * Adds additional integrity information to messages to prevent replay-attacks.
 *
 * The timestamp in the message can be compared to the timestamp of the last message the user sent. An example how this
 * can be done is in @link{org.openengsb.core.common.security.filter.MessageVerifierFilter}
 */
public abstract class AbstractSecureMessage<MessageType> implements Serializable {

    private static final long serialVersionUID = 8482667667480837182L;

    protected MessageType message;
    protected Long timestamp;

    public MessageType getMessage() {
        return this.message;
    }

    public void setMessage(MessageType message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
