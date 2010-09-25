package org.openengsb.core.persistence;

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
        if (!(obj instanceof PersistenceTestBean)) {
            return false;
        }
        PersistenceTestBean other = (PersistenceTestBean) obj;
        return safeEquals(this.intValue, other.intValue) && safeEquals(this.stringValue, other.stringValue)
                && this.reference == other.reference;
    }

    private boolean safeEquals(Object a, Object b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
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
        hash += 13 * System.identityHashCode(reference);
        return hash;
    }

}
