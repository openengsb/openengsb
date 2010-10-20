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

package org.openengsb.core.persistence;

import org.apache.commons.lang.ObjectUtils;

public class PersistenceTestBean {

    private String stringValue;

    private Integer intValue;

    private PersistenceTestBean reference;

    public PersistenceTestBean(String stringValue, Integer intValue, PersistenceTestBean reference) {
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.reference = reference;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public PersistenceTestBean getReference() {
        return reference;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public void setReference(PersistenceTestBean reference) {
        this.reference = reference;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PersistenceTestBean)) {
            return false;
        }
        PersistenceTestBean other = (PersistenceTestBean) obj;
        return ObjectUtils.equals(this.intValue, other.intValue)
                && ObjectUtils.equals(this.stringValue, other.stringValue);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        if (intValue != null) {
            hash += 13 * intValue;
        }
        if (stringValue != null) {
            hash += 13 * stringValue.hashCode();
        }
        return hash;
    }

}
