/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.web.testClient;

import org.apache.commons.lang.ObjectUtils;

public class TestBean {
    private String id;
    private String name;

    public TestBean() {
    }

    public TestBean(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ObjectUtils.hashCode(this.id);
        result = prime * result + ObjectUtils.hashCode(this.name);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TestBean)) {
            return false;
        }
        TestBean other = (TestBean) obj;
        return ObjectUtils.equals(this.id, other.id) && ObjectUtils.equals(this.name, other.name);
    }

}
