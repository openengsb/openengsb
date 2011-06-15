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

package org.openengsb.ports.ws;

import java.util.HashMap;

import javax.jws.WebService;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.openengsb.core.common.remote.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default handler for all incoming WS messages. This one is not meant to be replaced by a different implementation
 * though.
 */
@WebService(endpointInterface = "org.openengsb.ports.ws.PortReceiver")
public class DefaultPortReceiver implements PortReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPortReceiver.class);

    private FilterChain filterChain;

    public DefaultPortReceiver() {
    }

    public DefaultPortReceiver(FilterChain filterChain) {
        this.filterChain = filterChain;
    }

    @Override
    public String receive(String message) {
        HashMap<String, Object> metadata = new HashMap<String, Object>();
        String result;
        try {
            LOGGER.debug("starting filterchain for incoming message");
            result = (String) filterChain.filter(message, metadata);
        } catch (Exception e) {
            LOGGER.error("an error occured when processing the filterchain", e);
            result = ExceptionUtils.getStackTrace(e);
        }
        return result;
    }
}
