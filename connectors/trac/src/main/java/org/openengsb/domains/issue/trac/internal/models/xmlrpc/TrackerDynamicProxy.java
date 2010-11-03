/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.issue.trac.internal.models.xmlrpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.common.TypeConverter;
import org.apache.xmlrpc.common.TypeConverterFactory;
import org.apache.xmlrpc.common.TypeConverterFactoryImpl;

/**
 * This class is copied and slightly modified from the Trac XML-RPC Plugin Java example
 * (http://trac-hacks.org/wiki/XmlRpcPlugin#UsingfromJava). See NOTICE for further details
 */
public class TrackerDynamicProxy {
    private final XmlRpcClient client;
    private final TypeConverterFactory typeConverterFactory;
    private boolean objectMethodLocal;

    /**
     * Creates a new instance.
     *
     * @param client A fully configured XML-RPC client, which is used internally to perform XML-RPC calls.
     * @param typeConverterFactory Creates instances of {@link TypeConverterFactory}, which are used to transform the
     *        result object in its target representation.
     */
    public TrackerDynamicProxy(XmlRpcClient client, TypeConverterFactory typeConverterFactory) {
        this.typeConverterFactory = typeConverterFactory;
        this.client = client;
    }

    /**
     * Creates a new instance. Shortcut for
     * <p/>
     *
     * <pre>
     * new ClientFactory(pClient, new TypeConverterFactoryImpl());
     * </pre>
     *
     * @param client A fully configured XML-RPC client, which is used internally to perform XML-RPC calls.
     * @see TypeConverterFactoryImpl
     */
    public TrackerDynamicProxy(XmlRpcClient client) {
        this(client, new TypeConverterFactoryImpl());
    }

    /**
     * Returns the factories client.
     */
    public XmlRpcClient getClient() {
        return client;
    }

    /**
     * Returns, whether a method declared by the {@link Object Object class} is performed by the local object, rather
     * than by the server. Defaults to true.
     */
    public boolean isObjectMethodLocal() {
        return objectMethodLocal;
    }

    /**
     * Sets, whether a method declared by the {@link Object Object class} is performed by the local object, rather than
     * by the server. Defaults to true.
     */
    public void setObjectMethodLocal(boolean objectMethodLocal) {
        this.objectMethodLocal = objectMethodLocal;
    }

    /**
     * Creates an object, which is implementing the given interface. The objects methods are internally calling an
     * XML-RPC server by using the factories client.
     */
    public <T> Object newInstance(Class<T> clazz) {
        return newInstance(Thread.currentThread().getContextClassLoader(), clazz);
    }

    /**
     * Creates an object, which is implementing the given interface. The objects methods are internally calling an
     * XML-RPC server by using the factories client.
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance(ClassLoader classLoader, final Class<T> clazz) {
        return (T) Proxy.newProxyInstance(classLoader, new Class[]{ clazz }, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args)
                throws InvocationTargetException, IllegalAccessException, XmlRpcException {
                if (isObjectMethodLocal() && method.getDeclaringClass().equals(Object.class)) {
                    return method.invoke(proxy, args);
                }

                String classname = clazz.getName().replaceFirst(clazz.getPackage().getName() + ".", "").toLowerCase();

                classname = classname.replace("$", "."); // dirty hack TODO

                String methodName = classname + "." + method.getName();
                Object result = client.execute(methodName, args);
                TypeConverter typeConverter = typeConverterFactory.getTypeConverter(method.getReturnType());
                return typeConverter.convert(result);
            }
        });
    }
}
