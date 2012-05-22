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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * General model describing the configuration required for rules.
 */
// TODO: [OPENENGSB-1253] Design Rule Configuration object
@XmlRootElement
public class RuleConfiguration extends ConfigItem<String> {

    private static final long serialVersionUID = 5451541282607978079L;

    public static final String TYPE_ID = "RULE";

    public RuleConfiguration() {
        super();
    }

    public RuleConfiguration(Map<String, String> metaData, String ruleContent) {
        super(metaData, ruleContent);
    }

}
