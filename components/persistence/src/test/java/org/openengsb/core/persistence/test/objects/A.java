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

package org.openengsb.core.persistence.test.objects;

import java.io.Serializable;

public class A implements Z, Serializable {

    private static final long serialVersionUID = 6358610617115942596L;

    private String blub;
    private String additionalValue = null;

    public A() {
    }

    public A(String blub) {
        this.blub = blub;
    }

    public String getBlub() {
        return blub;
    }

    public void setBlub(String blub) {
        this.blub = blub;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof A)) {
            return false;
        }
        if (blub == null) {
            return true;
        }
        return blub.equals(((A) obj).blub);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getAdditionalValue() {
        return additionalValue;
    }

}
