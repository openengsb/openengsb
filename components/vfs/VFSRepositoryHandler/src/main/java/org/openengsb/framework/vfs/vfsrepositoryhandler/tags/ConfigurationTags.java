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

package org.openengsb.framework.vfs.vfsrepositoryhandler.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openengsb.framework.vfs.api.common.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationTags {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationTags.class);
    private List<Tag> tags;

    public ConfigurationTags() {
        tags = new ArrayList<Tag>();
    }

    public List<Tag> getTags() {
        Collections.sort(tags);
        return tags;
    }

    public void addTag(Tag tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
            Collections.sort(tags);
        }
    }

    @Override
    public boolean equals(Object that) {
        if (that == null) {
            return false;
        }

        if (!(that instanceof ConfigurationTags)) {
            return false;
        }

        ConfigurationTags thatTags = (ConfigurationTags) that;

        List<Tag> thisTagList = getTags();
        List<Tag> thatTagList = thatTags.getTags();

        if (thisTagList.size() != thatTagList.size()) {
            return false;
        }

        for (int i = 0; i < thisTagList.size(); i++) {
            if (!thisTagList.get(i).equals(thatTagList.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public Tag getPreviousTag(Tag tag) {
        Collections.sort(tags);

        int index = tags.indexOf(tag);

        if (index > 0) {
            return tags.get(index - 1);
        }

        return null;
    }
}
