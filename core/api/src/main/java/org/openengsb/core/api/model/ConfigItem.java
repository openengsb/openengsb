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

import java.util.Properties;

/**
 * General ConfigurationObject which could not be instanciated by itself, but rather have to be implemented in a
 * ConfigurationObject.
 */
// TODO: Implement
public abstract class ConfigItem<ConfigType> {

    private Properties metaData;
    private ConfigType content;

    public ConfigItem() {
    }

    public ConfigItem(Properties metaData, ConfigType content) {
        this.metaData = metaData;
        this.content = content;
    }

    public Properties getMetaData() {
        return metaData;
    }

    public void setMetaData(Properties metaData) {
        this.metaData = metaData;
    }

    public ConfigType getContent() {
        return content;
    }

    public void setContent(ConfigType content) {
        this.content = content;
    }

}
