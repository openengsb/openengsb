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
import java.util.Set;

/**
 * General ConfigurationObject which should not be instanciated by itself, but rather have to be implemented in a
 * ConfigurationObject.
 */
public class ConfigItem<ContentType> {

    protected Map<String, String> metaData;
    protected ContentType content;

    public ConfigItem() {
    }

    public ConfigItem(Map<String, String> metaData, ContentType content) {
        this.metaData = metaData;
        this.content = content;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public ContentType getContent() {
        return content;
    }

    public void setContent(ContentType content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ConfigItem)) {
            return false;
        }
        ConfigItem<?> toCompare = (ConfigItem<?>) obj;
        if (!compareMetadata(toCompare.getMetaData(), getMetaData())) {
            if (!compareMetadata(getMetaData(), toCompare.getMetaData())) {
                return false;
            }
        }
        return true;
    }

    private boolean compareMetadata(Map<String, String> first, Map<String, String> second) {
        Set<String> keys = first.keySet();
        for (String key : keys) {
            if (!second.containsKey(key) || !second.get(key).equals(first.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
