/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.swingclient;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import org.openengsb.contextcommon.ContextHelperExtended;
import org.openengsb.core.model.MethodCall;
import org.openengsb.core.model.ReturnValue;
import org.openengsb.core.transformation.Transformer;
import org.openengsb.util.serialization.SerializationException;

public class ContextFacade {

    public void remove(String name) {
        try {
            List<String> toRemove = new ArrayList<String>();
            toRemove.add(name);
            String xml = getMessage("remove", new Object[] { toRemove }, List.class);
            OpenEngSBClient.contextCall(xml);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(String key, String oldValue, String newValue) {
        if (oldValue != null) {
            String value = getValue(key);

            if (!value.equals(oldValue)) {
                throw new ConcurrentModificationException();
            }
        }

        try {
            Map<String, String> toStore = new HashMap<String, String>();
            toStore.put(key, newValue);
            String xml = getMessage("store", new Object[] { toStore }, Map.class);
            OpenEngSBClient.contextCall(xml);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public void createContext(String key) {
        try {
            List<String> toCreate = new ArrayList<String>();
            toCreate.add(key);
            String xml = getMessage("addEmptyContext", new Object[] { toCreate }, List.class);
            OpenEngSBClient.contextCall(xml);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValue(String pathAndKey) {
        try {
            String xml = getMessage("getValue", new Object[] { pathAndKey }, String.class);
            String result = OpenEngSBClient.contextCall(xml);
            ReturnValue returnValue = Transformer.toReturnValue(result);
            return (String) returnValue.getValue();
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private String getMessage(String methodName, Object[] args, Class<?>... argClasses) {
        try {
            Method method = ContextHelperExtended.class.getMethod(methodName, argClasses);
            MethodCall call = new MethodCall(method, args);
            String xml = Transformer.toXml(call);
            return xml;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
