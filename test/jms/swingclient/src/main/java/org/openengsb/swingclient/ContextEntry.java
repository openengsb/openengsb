package org.openengsb.swingclient;

public class ContextEntry {

    private String path;
    private String name;
    private String value;

    public ContextEntry(String path, String name, String value) {
        this.path = path;
        this.name = name;
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
