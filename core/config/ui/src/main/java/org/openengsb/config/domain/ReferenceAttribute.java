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
import javax.persistence.ManyToOne;

@Entity
@SuppressWarnings("serial")
public class ReferenceAttribute extends Attribute {
    @ManyToOne
    private PersistedObject reference;

    public ReferenceAttribute() {
    }

    public ReferenceAttribute(PersistedObject parent, String key, PersistedObject reference) {
        super(parent, key);
        this.reference = reference;
    }

    public PersistedObject getReference() {
        return reference;
    }

    public void setReference(PersistedObject reference) {
        this.reference = reference;
    }

    @Override
    protected String toStringValue() {
        return reference != null ? reference.getName() : "";
    }
}
