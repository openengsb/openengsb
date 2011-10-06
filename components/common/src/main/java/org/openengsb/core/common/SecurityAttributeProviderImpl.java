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

package org.openengsb.core.common;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.openengsb.core.api.security.SecurityAttributeProvider;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SecurityAttributeProviderImpl implements SecurityAttributeProvider {

    private Map<Object, Collection<SecurityAttributeEntry>> data = Maps.newHashMap();

    @Override
    public Collection<SecurityAttributeEntry> getAttribute(Object o) {
        if (!data.containsKey(o)) {
            return Collections.emptySet();
        }
        return data.get(o);
    }

    public void putAttribute(Object key, SecurityAttributeEntry... values) {
        if (!data.containsKey(key)) {
            data.put(key, new HashSet<SecurityAttributeEntry>());
        }
        CollectionUtils.addAll(data.get(key), values);
    }

    public void clearAttributes(Object key) {
        data.get(key).clear();
    }

    public void replaceAttributes(Object key, SecurityAttributeEntry... values) {
        data.put(key, Sets.newHashSet(values));
    }

}
