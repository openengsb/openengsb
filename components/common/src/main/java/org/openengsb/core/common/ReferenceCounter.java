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
package org.openengsb.core.common;

import java.io.File;
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
        Set<T> valueSet = dataByFile.get(artifact);
        Set<T> garbage = Sets.newHashSet(valueSet);
        for (Iterator<T> iterator = valueSet.iterator(); iterator.hasNext();) {
            T r = iterator.next();

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
