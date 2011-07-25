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

package org.openengsb.core.api.edb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModel;

public class EDBBatchEvent extends EDBEvent {
    private Map<String, OpenEngSBModel> creates;
    private Map<String, OpenEngSBModel> updates;
    private List<String> deletes;
    
    public EDBBatchEvent() {
        creates = new HashMap<String, OpenEngSBModel>();
        updates = new HashMap<String, OpenEngSBModel>();
        deletes = new ArrayList<String>();
    }
    
    public void addModelCreate(String oid, OpenEngSBModel model) {
        creates.put(oid, model);
    }
    
    public void removeModelCreate(String oid) {
        creates.remove(oid);
    }
    
    public void addModelUpdate(String oid, OpenEngSBModel model) {
        updates.put(oid, model);
    }
    
    public void removeModelUpdate(String oid) {
        updates.remove(oid);
    }
    
    public void addModelDelete(String oid) {
        deletes.add(oid);
    }
    
    public void removeModelDelete(String oid) {
        deletes.remove(oid);
    }
    
    public Map<String, OpenEngSBModel> getCreations() {
        return creates;
    }
    
    public Map<String, OpenEngSBModel> getUpdates() {
        return updates;
    }
    
    public List<String> getDeletions() {
        return deletes;
    }

}
