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

package org.openengsb.core.ekb.internal;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

/**
 * little class to test if the querying of models also works with self implemented classes and not only with proxied
 * interfaces
 */
public class TestModel2 implements OpenEngSBModel {
    private String id;
    private String name;
    private Date date;
    private ENUM enumeration;

    enum ENUM {
            A,
            B,
            C
    }

    @Override
    public List<OpenEngSBModelEntry> getOpenEngSBModelEntries() {
        return Arrays.asList(new OpenEngSBModelEntry("id", id, String.class),
            new OpenEngSBModelEntry("name", name, String.class),
            new OpenEngSBModelEntry("date", date, Date.class),
            new OpenEngSBModelEntry("test", enumeration, ENUM.class));
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ENUM getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(ENUM enumeration) {
        this.enumeration = enumeration;
    }
}
