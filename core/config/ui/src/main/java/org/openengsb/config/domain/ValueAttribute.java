/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.config.domain;

import javax.persistence.Entity;

@Entity
@SuppressWarnings("serial")
public class ValueAttribute extends Attribute {
    private String value;

    public ValueAttribute() {
        value = "";
    }

    public ValueAttribute(PersistedObject parent, String key, String value) {
        super(parent, key);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected String toStringValue() {
        return value;
    }
}
