package org.openengsb.core.common.security;

import java.io.File;

public abstract class FileSource<V> {

    private File file;
    private long lastModified;
    private V value;

    public FileSource(File file) {
        this.file = file;
    }

    protected void update() {
        long lastModified = file.lastModified();
        if (lastModified == this.lastModified) {
            return;
        }
        this.lastModified = lastModified;
        this.value = updateValue(file);
    }

    protected abstract V updateValue(File file);

    public V getValue() {
        update();
        return value;
    }

}
