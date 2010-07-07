package org.openengsb.persistence;

public class PersistenceObject {

    private String xml;

    private String className;

    public PersistenceObject(String xml, String className) {
        this.xml = xml;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getXml() {
        return xml;
    }
}
