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

package org.openengsb.persistence;

import java.util.List;
import java.util.Map;

public class ComplexTestBean {
    public SimpleTestBean simple;
    public List<String> testList;
    public Map<Long, String> testMap;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.simple == null) ? 0 : this.simple.hashCode());
        result = prime * result + ((this.testList == null) ? 0 : this.testList.hashCode());
        result = prime * result + ((this.testMap == null) ? 0 : this.testMap.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ComplexTestBean other = (ComplexTestBean) obj;
        if (this.simple == null) {
            if (other.simple != null)
                return false;
        } else if (!this.simple.equals(other.simple))
            return false;
        if (this.testList == null) {
            if (other.testList != null)
                return false;
        } else if (!this.testList.equals(other.testList))
            return false;
        if (this.testMap == null) {
            if (other.testMap != null)
                return false;
        } else if (!this.testMap.equals(other.testMap))
            return false;
        return true;
    }

}
