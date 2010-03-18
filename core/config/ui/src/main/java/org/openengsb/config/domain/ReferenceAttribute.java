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
public class ReferenceAttribute extends Attribute {
    private static final long serialVersionUID = 1L;

    @ManyToOne
    private Endpoint reference;

    public ReferenceAttribute() {
    }

    public ReferenceAttribute(Endpoint parentEndpoint, String key, Endpoint reference) {
        super(parentEndpoint, key);
        this.reference = reference;
    }

    public Endpoint getReference() {
        return reference;
    }

    public void setReference(Endpoint reference) {
        this.reference = reference;
    }

    @Override
    protected String toStringValue() {
        return reference != null ? reference.toString() : "";
    }
}
