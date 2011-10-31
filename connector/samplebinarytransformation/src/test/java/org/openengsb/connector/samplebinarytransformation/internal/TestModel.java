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

package org.openengsb.connector.samplebinarytransformation.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

public class TestModel implements OpenEngSBModel {
    private Map<String, OpenEngSBModelEntry> entries;
    private Integer id;
    private String name;
    
    public TestModel() {
        entries = new HashMap<String, OpenEngSBModelEntry>();
        entries.put("id", new OpenEngSBModelEntry("id", id, Integer.class));
        entries.put("name", new OpenEngSBModelEntry("name", name, String.class));
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        entries.put("id", new OpenEngSBModelEntry("id", id, Integer.class));
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        entries.put("name", new OpenEngSBModelEntry("name", name, String.class));
        this.name = name;
    }

    @Override
    public List<OpenEngSBModelEntry> getOpenEngSBModelEntries() {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        entries.addAll(this.entries.values());
        return entries;
    }

    @Override
    public void addOpenEngSBModelEntry(OpenEngSBModelEntry entry) {
        entries.put(entry.getKey(), entry);
    }

    @Override
    public void removeOpenEngSBModelEntry(String key) {
        entries.remove(key);
    }

}
