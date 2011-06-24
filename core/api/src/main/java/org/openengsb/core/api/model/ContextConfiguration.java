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

package org.openengsb.core.api.model;

import java.util.Map;

import org.openengsb.core.api.context.Context;

/**
 * Specific configuration model for the configuration to get up an context.
 */
// TODO: [OPENENGSB-1252] Design context object
public class ContextConfiguration extends ConfigItem<Map<String, String>> {

    public static final String TYPE_ID = "CONTEXT";

    public ContextConfiguration() {
        super();
    }

    public ContextConfiguration(Map<String, String> metaData, Map<String, String> content) {
        super(metaData, content);
    }

    public Context toContext() {
        ContextId contextId = ContextId.fromMetaData(this.getMetaData());
        Context c = new Context();
        c.setId(contextId.getId());
        return c;
    }

}
