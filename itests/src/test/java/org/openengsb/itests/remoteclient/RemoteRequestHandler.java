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

package org.openengsb.itests.remoteclient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResult.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

class RemoteRequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteRequestHandler.class);
    private ExampleConnector connector = new ExampleConnector();

    private Map<MethodCall, MethodResult> invocationHistory = Maps.newLinkedHashMap();

    public MethodResult process(MethodCall request) {
        List<String> argClassList = request.getClasses();
        Class<?>[] argClasses = new Class<?>[argClassList.size()];
        LOGGER.debug("converting arguments");
        for (int i = 0; i < argClassList.size(); i++) {
            try {
                argClasses[i] = Class.forName(argClassList.get(i));
            } catch (ClassNotFoundException e) {
                return makeExceptionResult(new IllegalStateException(e));
            }
        }
        Method method;
        try {
            LOGGER.debug("searching for method {} with args {}", request.getMethodName(), request.getClasses());
            method = ExampleConnector.class.getMethod(request.getMethodName(), argClasses);
        } catch (NoSuchMethodException e) {
            return makeExceptionResult(e);
        }
        try {
            LOGGER.info("invoking method {}", method);
            Object result = method.invoke(connector, request.getArgs());
            if (method.getReturnType().equals(void.class)) {
                MethodResult methodResult = new MethodResult();
                methodResult.setClassName(Object.class.getName());
                methodResult.setType(ReturnType.Void);
                return methodResult;
            }
            LOGGER.debug("invocation successful");
            MethodResult methodResult = new MethodResult(result);
            invocationHistory.put(request, methodResult);
            return methodResult;
        } catch (InvocationTargetException e) {
            return makeExceptionResult((Exception) e.getTargetException());
        } catch (IllegalArgumentException e) {
            return makeExceptionResult(e);
        } catch (IllegalAccessException e) {
            return makeExceptionResult(new IllegalStateException(e));
        }
    }

    private MethodResult makeExceptionResult(Exception e) {
        LOGGER.error("Exception occured, making Exception result");
        return new MethodResult(Throwables.getStackTraceAsString(e), ReturnType.Exception);
    }

    public Map<MethodCall, MethodResult> getInvocationHistory() {
        return invocationHistory;
    }
}
