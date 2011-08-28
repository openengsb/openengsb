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

package org.openengsb.core.edb.internal;

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.edb.EDBObject;

/**
 * A JPA Head contains all JPAObjects which are bound to a specific timestamp.
 */
public class JPAHead {
    private List<JPAObject> objects;
    private Long timestamp;

    public JPAHead() {
    }

    public JPAHead(Long timestamp) {
        this.timestamp = timestamp;
        objects = new ArrayList<JPAObject>();
    }

    public int count() {
        return objects.size();
    }

    public List<EDBObject> getEDBObjects() {
        List<EDBObject> loaded = new ArrayList<EDBObject>();
        for (JPAObject o : objects) {
            loaded.add(o.getObject());
        }
        return loaded;
    }
    
    public void setJPAObjects(List<JPAObject> objects) {
        this.objects = objects;
    }

    public List<JPAObject> getJPAObjects() {
        return objects;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
