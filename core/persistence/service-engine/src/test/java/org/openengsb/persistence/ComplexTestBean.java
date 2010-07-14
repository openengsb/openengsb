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
