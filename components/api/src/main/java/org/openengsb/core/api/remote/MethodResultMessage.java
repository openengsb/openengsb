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

package org.openengsb.core.api.remote;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * Container for sending {@link MethodResult} to remote destinations. It contains the necessary routing- and
 * security-information.
 *
 */
@XmlRootElement
public class MethodResultMessage extends MessageBase {

    private static final long serialVersionUID = 6620312248741724626L;

    private MethodResult result;

    public MethodResultMessage() {
    }

    public MethodResultMessage(MethodResult result, String callId) {
        super(callId);
        this.result = result;
    }

    public MethodResult getResult() {
        return result;
    }

    public void setResult(MethodResult result) {
        this.result = result;
    }

}
