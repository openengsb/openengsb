package org.openengsb.core.common;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ReferenceCounter<T> {

    private Map<T, Set<File>> dataByValue = Maps.newHashMap();
    private Map<File, Set<T>> dataByFile = Maps.newHashMap();

    public void addReference(File artifact, T object) {
        Set<File> fileList = dataByValue.get(object);
        if (fileList == null) {
            fileList = Sets.newHashSet();
            dataByValue.put(object, fileList);
        }
        fileList.add(artifact);

        Set<T> valueList = dataByFile.get(artifact);
        if (valueList == null) {
            valueList = Sets.newHashSet();
            dataByFile.put(artifact, valueList);
        }
        valueList.add(object);
    }

    public void removeReference(File artifact, T object) {
        if (!dataByValue.containsKey(object)) {
            return;
        }
        if (!dataByFile.containsKey(artifact)) {
            return;
        }

        Set<T> collection = dataByFile.get(artifact);
        collection.remove(object);
        if (collection.isEmpty()) {
            dataByFile.remove(artifact);
        }

        Set<File> fileList = dataByValue.get(object);
        fileList.remove(artifact);
        if (fileList.isEmpty()) {
            dataByValue.remove(object);
        }
    }

    public Set<T> removeFile(File artifact) {
        Set<T> garbage = Sets.newHashSet(dataByFile.get(artifact));
        for (Iterator<T> iterator = garbage.iterator(); iterator.hasNext();) {
            T r = (T) iterator.next();

            Set<File> fileList = dataByValue.get(r);
            fileList.remove(artifact);

            if (fileList.isEmpty()) {
                iterator.remove();
            } else {
                garbage.remove(r);
            }
        }
        return garbage;
    }

}
