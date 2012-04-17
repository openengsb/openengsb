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
package org.openengsb.core.common.remote;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.DomHandler;
import javax.xml.bind.annotation.W3CDomHandler;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.model.core.ErrorHandler;

public final class CustomAnnotationReader implements RuntimeAnnotationReader {

    @Override
    public void setErrorHandler(ErrorHandler errorHandler) {
    }

    @Override
    public boolean hasMethodAnnotation(Class<? extends Annotation> annotation, String propertyName, Method getter,
            Method setter, Locatable srcPos) {
        return hasMethodAnnotation(annotation, getter) || hasMethodAnnotation(annotation, getter);
    }

    @Override
    public boolean hasMethodAnnotation(Class<? extends Annotation> annotation, Method method) {
        return getMethodAnnotation(annotation, method, null) != null;
    }

    @Override
    public boolean hasFieldAnnotation(Class<? extends Annotation> annotationType, Field field) {
        return getFieldAnnotation(annotationType, field, null) != null;
    }

    @Override
    public boolean hasClassAnnotation(@SuppressWarnings("rawtypes") Class clazz,
            Class<? extends Annotation> annotationType) {
        return getClassAnnotation(annotationType, clazz, null) != null;
    }

    @Override
    public <A extends Annotation> A getPackageAnnotation(Class<A> annotation,
            @SuppressWarnings("rawtypes") Class clazz, Locatable srcpos) {
        return null;
    }

    @Override
    public <A extends Annotation> A getMethodParameterAnnotation(Class<A> annotation, Method method,
            int paramIndex,
            Locatable srcPos) {
        return null;
    }

    @Override
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotation, Method getter, Method setter,
            Locatable srcpos) {
        A result = getMethodAnnotation(annotation, getter, srcpos);
        if (result != null) {
            return result;
        }
        return getMethodAnnotation(annotation, setter, srcpos);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A getMethodAnnotation(Class<A> annotation, Method method, Locatable srcpos) {
        Annotation[] annotations = getAllMethodAnnotations(method, srcpos);
        for (Annotation a : annotations) {
            if (annotation.isInstance(a)) {
                return (A) a;
            }
        }
        return null;
        // if (!methodAnnotations.containsKey(method)) {
        // return null;
        // }
        // Collection<Annotation> annoatations = methodAnnotations.get(method);
        // try {
        // return (A) Iterators.find(annoatations.iterator(), Predicates.instanceOf(annotation));
        // } catch (NoSuchElementException e) {
        // return null;
        // }
    }

    @Override
    public <A extends Annotation> A getFieldAnnotation(Class<A> annotation, final Field field, Locatable srcpos) {
        return null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class getClassValue(Annotation a, String name) {
        try {
            return (Class) a.annotationType().getMethod(name).invoke(a);
        } catch (IllegalAccessException e) {
            // impossible
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            // impossible
            throw new InternalError(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class[] getClassArrayValue(Annotation a, String name) {
        try {
            return (Class[]) a.annotationType().getMethod(name).invoke(a);
        } catch (IllegalAccessException e) {
            // impossible
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            // impossible
            throw new InternalError(e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A getClassAnnotation(Class<A> annotation,
            @SuppressWarnings("rawtypes") final Class clazz, Locatable srcpos) {
        if (annotation.equals(XmlRootElement.class) && !clazz.getName().startsWith("java")
                && !isAbstractOrInterface(clazz)) {
            System.out.println("XmlRoot: " + clazz);
            return (A) CustomAnnotationReader.makeXmlRootAnnoatation("##default");
        }
        return null;
    }

    @Override
    public Annotation[] getAllMethodAnnotations(Method method, Locatable srcPos) {
        if (!method.getName().startsWith("get")) {
            return new Annotation[0];
        }
        String propname = method.getName().substring(3);
        propname = StringUtils.uncapitalize(propname);
        Class<?> propType = method.getReturnType();

        if (Map.class.isAssignableFrom(propType)) {
            return new Annotation[0];
        }

        if (propType.isArray() || Collection.class.isAssignableFrom(propType)) {
            System.out.println("xmlElementWrapper: " + method);
            return new Annotation[]{
                CustomAnnotationReader.makeXmlElementWrapper(propname),
                CustomAnnotationReader.makeXmlElement(propname.replaceAll("s$", "")),
            };
        }

        if (isAbstractOrInterface(propType)) {
            System.out.println("xmlanyElement: " + method);
            return new Annotation[]{
                CustomAnnotationReader.makeXmlAnyElement(),
            };
        }

        return new Annotation[0];

        // if (!methodAnnotations.containsKey(method)) {
        // return new Annotation[0];
        // }
        // return methodAnnotations.get(method).toArray(new Annotation[0]);

    }

    private boolean isAbstractOrInterface(Class<?> propType) {
        return Modifier.isAbstract(propType.getModifiers()) || Modifier.isInterface(propType.getModifiers());
    }

    @Override
    public Annotation[] getAllFieldAnnotations(Field field, Locatable srcPos) {
        return new Annotation[0];
    }

    static Annotation makeXmlElementWrapper(final String name) {
        return new XmlElementWrapper() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return XmlElementWrapper.class;
            }

            @Override
            public boolean required() {
                return false;
            }

            @Override
            public boolean nillable() {
                return false;
            }

            @Override
            public String namespace() {
                return "##default";
            }

            @Override
            public String name() {
                return name;
            }
        };
    }

    static Annotation makeXmlAnyElement() {
        return new XmlAnyElement() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return XmlAnyElement.class;
            }

            @SuppressWarnings("rawtypes")
            @Override
            public Class<? extends DomHandler> value() {
                return W3CDomHandler.class;
            }

            @Override
            public boolean lax() {
                return false;
            }
        };
    }

    static Annotation makeXmlElement(final String name) {
        return new XmlElement() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return XmlElement.class;
            }

            @SuppressWarnings("rawtypes")
            @Override
            public Class type() {
                return DEFAULT.class;
            }

            @Override
            public boolean required() {
                return false;
            }

            @Override
            public boolean nillable() {
                return false;
            }

            @Override
            public String namespace() {
                return "##default";
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String defaultValue() {
                return "\u0000";
            }
        };
    }

    static Annotation makeXmlRootAnnoatation(final String name) {
        return new XmlRootElement() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return XmlRootElement.class;
            }

            @Override
            public String namespace() {
                return "##default";
            }

            @Override
            public String name() {
                return name;
            }
        };
    }
}
