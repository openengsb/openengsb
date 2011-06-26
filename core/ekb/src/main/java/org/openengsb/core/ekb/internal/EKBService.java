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

package org.openengsb.core.ekb.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.ekb.EngineeringKnowlegeBaseService;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service which implements the EngineeringKnowlegeBaseService. Also represents a proxy for simulating simple
 * OpenEngSBModel interfaces.
 */
public class EKBService implements EngineeringKnowlegeBaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EKBService.class);

    @SuppressWarnings("unused")
    private EngineeringDatabaseService edbService;

    @Override
    public Object createModelObject(Class<?> model, OpenEngSBModelEntry... entries) throws IllegalArgumentException {
        LOGGER.debug("createModelObject for model interface {} called", model.getName());

        boolean valid = false;

        for (Class<?> i : model.getInterfaces()) {
            if (i.equals(OpenEngSBModel.class)) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            throw new IllegalArgumentException("the model class is not an interface of OpenEngSBModel");
        }
        
        return generateProxy(model, entries);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T extends OpenEngSBModel> T createEmptyModelObject(Class<T> model, OpenEngSBModelEntry... entries) {
        LOGGER.debug("createEmpytModelObject for model interface {} called", model.getName());

        return (T) generateProxy(model, entries);
    }
    
    private Object generateProxy(Class<?> model, OpenEngSBModelEntry... entries) {
        ClassLoader classLoader = model.getClassLoader();
        Class<?>[] classes = new Class<?>[]{ OpenEngSBModel.class, model };
        InvocationHandler handler = makeHandler(model.getMethods(), entries);

        return Proxy.newProxyInstance(classLoader, classes, handler);
    }

    private EKBProxyHandler makeHandler(Method[] methods, OpenEngSBModelEntry[] entries) {
        EKBProxyHandler handler = new EKBProxyHandler(methods, entries);
        return handler;
    }

    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }
}
