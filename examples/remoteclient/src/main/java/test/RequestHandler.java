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

package test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang.ArrayUtils;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResult.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

class RequestHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);
    private ExampleConnector connector = new ExampleConnector();

    public MethodResult process(MethodCall request) {
        Object[] args = request.getArgs();
        Class<?>[] argClasses = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }
        Method method;
        try {
            LOGGER.debug("searching for method {} with args {}", request.getMethodName(),
                ArrayUtils.toString(argClasses));
            method = ExampleConnector.class.getMethod(request.getMethodName(), argClasses);
        } catch (NoSuchMethodException e) {
            return makeExceptionResult(e);
        }
        try {
            LOGGER.info("invoking method {}", method);
            Object result = method.invoke(connector, request.getArgs());
            if (method.getReturnType().equals(void.class)) {
                MethodResult methodResult = new MethodResult();
                methodResult.setType(ReturnType.Void);
                return methodResult;
            }
            LOGGER.debug("invocation successful");
            return new MethodResult(result);
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
}
