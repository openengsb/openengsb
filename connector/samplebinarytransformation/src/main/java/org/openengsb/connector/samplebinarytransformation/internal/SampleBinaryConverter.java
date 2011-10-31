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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SampleBinaryConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleBinaryConverter.class);
    private File[] configs;
    private Class<?> clasz;

    public SampleBinaryConverter(Class<?> clasz, File... configs) {
        this.configs = configs;
        this.clasz = clasz;
    }

    public List<OpenEngSBModelEntry> convertToOpenEngSBModelEntries(Object object) {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        // just for test reason
        entries = exampleConvertToOpenEngSBModelEntries(object);
        // here should the real converting work be done
        return entries;
    }
    
    private List<OpenEngSBModelEntry> exampleConvertToOpenEngSBModelEntries(Object object) {
        if (OpenEngSBModel.class.isAssignableFrom(object.getClass())) {
            return ((OpenEngSBModel) object).getOpenEngSBModelEntries();
        }
        
        return new ArrayList<OpenEngSBModelEntry>();
    }

    public Object convertFromOpenEngSBModelEntries(List<OpenEngSBModelEntry> entries) {
        Object object = new Object();
        // for test reason:
        object = exampleConvertFromOpenEngSBModelEntries(entries);
        // here should the real converting work be done
        return object;
    }
    
    
    private Object exampleConvertFromOpenEngSBModelEntries(List<OpenEngSBModelEntry> entries) {
        Object object = null;
        try {
            object = clasz.newInstance();
        } catch (InstantiationException e) {
            LOGGER.error("Unable to create new instance of class {}", clasz.getName());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LOGGER.error("Unable to create new instance of class {}", clasz.getName());
            e.printStackTrace();
        }
        // just for test reason
        for (OpenEngSBModelEntry entry : entries) {
            StringBuilder builder = new StringBuilder("set");
            builder.append(Character.toUpperCase(entry.getKey().charAt(0)));
            builder.append(entry.getKey().substring(1));
            String methodName = builder.toString();
            try {
                Method method = clasz.getMethod(methodName, entry.getValue().getClass());
                method.invoke(object, entry.getValue());
            } catch (SecurityException e) {
                LOGGER.error("unable to set value for entry {}", entry.getKey());
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                LOGGER.error("unable to set value for entry {}", entry.getKey());
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                LOGGER.error("unable to set value for entry {}", entry.getKey());
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                LOGGER.error("unable to set value for entry {}", entry.getKey());
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                LOGGER.error("unable to set value for entry {}", entry.getKey());
                e.printStackTrace();
            }
        }
        
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
