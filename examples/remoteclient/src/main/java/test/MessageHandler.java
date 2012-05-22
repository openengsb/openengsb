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

package test;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResultMessage;

import com.google.common.base.Throwables;

class MessageHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    protected MessageHandler() {
    }

    public MethodCallRequest unmarshal(String text) {
        try {
            return MAPPER.readValue(text, MethodCallRequest.class);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public String marshal(MethodResultMessage methodResultMessage) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(methodResultMessage);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
