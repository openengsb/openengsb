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

package org.openengsb.connector.samplebinarytransformation.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModelEntry;

public class SampleBinaryConverter {
    private File[] configs;

    public SampleBinaryConverter(File... configs) {
        this.configs = configs;
    }

    public List<OpenEngSBModelEntry> convertToOpenEngSBModelEntries(Object object) {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        // here should the real converting work be done
        return entries;
    }

    public Object convertFromOpenEngSBModelEntries(List<OpenEngSBModelEntry> entries) {
        Object object = new Object();
        // here should the real converting work be done
        return object;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (File f : configs) {
            if (buffer.length() != 0) {
                buffer.append(", ");
            }
            buffer.append(f.getName());
        }
        return buffer.toString();
    }

}
