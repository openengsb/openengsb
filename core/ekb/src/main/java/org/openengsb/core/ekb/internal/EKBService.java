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
import java.lang.reflect.Proxy;

import org.openengsb.core.api.edb.EnterpriseDatabaseService;
import org.openengsb.core.api.ekb.EngineeringKnowlegeBaseService;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service which implements the EngineeringKnowlegeBaseService. Also represents a proxy for simulating simple
 * OpenEngSBModel interfaces.
 */
public class EKBService implements EngineeringKnowlegeBaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EKBService.class);

    @SuppressWarnings("unused")
    private EnterpriseDatabaseService edbService;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends OpenEngSBModel> T createModelObject(Class<T> model) {
        LOGGER.debug("createModelObject for model interface {} called", model.getName());

        ClassLoader classLoader = model.getClassLoader();
        Class<?>[] classes = new Class<?>[]{ OpenEngSBModel.class, model };
        InvocationHandler handler = makeHandler();

        return (T) Proxy.newProxyInstance(classLoader, classes, handler);
    }
    
    private EKBProxyHandler makeHandler() {
        EKBProxyHandler handler = new EKBProxyHandler();
        return handler;
    }
    
    public void setEdbService(EnterpriseDatabaseService edbService) {
        this.edbService = edbService;
    }
}
