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

import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * Represents a batch possibility to send a list of insert, delete and create commands. The reason for this batch event
 * is that it make it possible to do several inserts, updates and deletes in one commit of the EDB. If you use the other
 * three events, every event will be handled in an own EDB commit.
 */
public interface EDBBatchEvent extends EDBEvent {
    
    List<OpenEngSBModel> getInserts();
    
    void setInserts(List<OpenEngSBModel> inserts);
    
    List<OpenEngSBModel> getUpdates();
    
    void setUpdates(List<OpenEngSBModel> updates);
    
    List<OpenEngSBModel> getDeletes();
    
    void setDeletes(List<OpenEngSBModel> deletes);
    

    // private List<OpenEngSBModel> inserts;
    // private List<OpenEngSBModel> updates;
    // private List<OpenEngSBModel> deletes;
    //
    // public EDBBatchEvent() {
    // inserts = new ArrayList<OpenEngSBModel>();
    // updates = new ArrayList<OpenEngSBModel>();
    // deletes = new ArrayList<OpenEngSBModel>();
    // }
    //
    // public void addModelInsert(OpenEngSBModel model) {
    // inserts.add(model);
    // }
    //
    // public void addModelUpdate(OpenEngSBModel model) {
    // updates.add(model);
    // }
    //
    // public void addModelDelete(OpenEngSBModel model) {
    // deletes.add(model);
    // }
    //
    // public List<OpenEngSBModel> getInserts() {
    // return inserts;
    // }
    //
    // public List<OpenEngSBModel> getUpdates() {
    // return updates;
    // }
    //
    // public List<OpenEngSBModel> getDeletions() {
    // return deletes;
    // }

}
