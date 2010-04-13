/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.openengsb.contextcommon;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.openengsb.core.MessageProperties;
import org.openengsb.core.MethodCallHelper;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;

public class ContextHelperImpl implements ContextHelperExtended {

    private final OpenEngSBEndpoint endpoint;
    private MessageProperties msgProperties;

    public ContextHelperImpl(OpenEngSBEndpoint endpoint, MessageProperties msgProperties) {
        this.endpoint = endpoint;
        this.msgProperties = msgProperties;
    }
    
    public void setContext(String context) {
        this.msgProperties = new MessageProperties(context, null);
    }

    @Override
    public void addEmptyContext(List<String> paths) {
        QName contextEndpoint = getContextEndpoint();
        Method method = getMethod("addEmptyContext", List.class);
        MethodCallHelper.sendMethodCall(endpoint, contextEndpoint, method, new Object[] { paths }, msgProperties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getAllValues(String path) {
        QName contextEndpoint = getContextEndpoint();
        Method method = getMethod("getAllValues", String.class);
        return (Map<String, String>) MethodCallHelper.sendMethodCall(endpoint, contextEndpoint, method,
                new Object[] { path }, msgProperties);
    }

    @Override
    public String getValue(String pathAndKey) {
        QName contextEndpoint = getContextEndpoint();
        Method method = getMethod("getValue", String.class);
        return (String) MethodCallHelper.sendMethodCall(endpoint, contextEndpoint, method, new Object[] { pathAndKey },
                msgProperties);
    }

    @Override
    public void remove(List<String> paths) {
        QName contextEndpoint = getContextEndpoint();
        Method method = getMethod("remove", List.class);
        MethodCallHelper.sendMethodCall(endpoint, contextEndpoint, method, new Object[] { paths }, msgProperties);
    }

    @Override
    public void store(Map<String, String> values) {
        QName contextEndpoint = getContextEndpoint();
        Method method = getMethod("store", Map.class);
        MethodCallHelper.sendMethodCall(endpoint, contextEndpoint, method, new Object[] { values }, msgProperties);
    }

    @Override
    public Context getContext(String path) {
        QName contextEndpoint = getContextEndpoint();
        Method method = getMethod("getContext", String.class);
        return (Context) MethodCallHelper.sendMethodCall(endpoint, contextEndpoint, method, new Object[] { path },
                msgProperties);
    }

    private Method getMethod(String name, Class<?>... params) {
        try {
            return getClass().getMethod(name, params);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private QName getContextEndpoint() {
        return new QName("urn:openengsb:context", "contextService");
    }

}
