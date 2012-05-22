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

package org.openengsb.core.persistence.internal;

import static java.lang.String.format;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.collect.Lists;

/**
 * Object indexing internal classes.
 */
public class ObjectInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String realClass;
    private String location;
    private ArrayList<String> types;

    public ObjectInfo(String realClass, String location) {
        this.realClass = realClass;
        this.location = location;
        types = Lists.newArrayList();
    }

    public void addType(Class<?> type) {
        types.add(type.getName());
    }

    public String getLocation() {
        return location;
    }

    public String getRealClass() {
        return realClass;
    }

    @Override
    public String toString() {
        return format("ObjectInfo with location: %s, root type: %s and all types: %s", location, realClass,
            Arrays.toString(types.toArray()));
    }

    public boolean containsClass(Class<?> beanClass) {
        for (String type : types) {
            if (type.equals(beanClass.getName())) {
                return true;
            }
        }
        return false;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
