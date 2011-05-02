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

import org.apache.commons.codec.digest.DigestUtils;

public abstract class AbstractSecureMessage<MessageType> implements Serializable {

    private static final long serialVersionUID = 8482667667480837182L;

    protected MessageType message;
    protected Long timestamp;
    protected byte[] verification;

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

    public byte[] getVerification() {
        return this.verification;
    }

    public void setVerification(byte[] verification) {
        this.verification = verification;
    }

    public byte[] calcChecksum() {
        String concat = this.getMessage().toString() + this.timestamp;
        byte[] checksum = DigestUtils.sha(concat);
        return checksum;
    }

    protected void setVerification() {
        this.verification = calcChecksum();
    }

    // public void verify() {
    // byte[] refChecksum = calcChecksum(this.getMessage().toString(), this.getTimestamp());
    // if (!ArrayUtils.isEquals(this.verification, refChecksum)) {
    // throw new MessageVerificationFailedException("wrong checksum");
    // }
    // }

}
