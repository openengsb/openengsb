package org.openengsb.core.weaver.service.internal.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;

/**
 * ReflectionModelProxy
 */
public class ReflectionModelProxy implements OpenEngSBModel {

    private Object object;
    private Class<?> type;

    private BeanInfo beanInfo;
    private PropertyDescriptor id;

    private List<OpenEngSBModelEntry> tail;

    public synchronized static <T> T proxy(final T object, Class<T> clazz) {
        ProxyFactory factory = new ProxyFactory();

        factory.setSuperclass(clazz);
        factory.setInterfaces(new Class[]{ OpenEngSBModel.class });

        try {
            return clazz.cast(factory.create(new Class[0], new Object[0], new MethodHandler() {

                final ReflectionModelProxy reflectionModel = new ReflectionModelProxy(object);

                @Override
                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                    if (thisMethod.getDeclaringClass().equals(OpenEngSBModel.class)) {
                        return thisMethod.invoke(reflectionModel, args);
                    }

                    return proceed.invoke(self, args);
                }
            }));
        } catch (IntrospectionException | InvocationTargetException | NoSuchMethodException | InstantiationException
                | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized static OpenEngSBModel proxy(final Object object) {
        return (OpenEngSBModel) proxy(object, (Class<Object>) object.getClass());
    }

    private ReflectionModelProxy(Object object) throws IntrospectionException {
        this.object = object;
        this.type = object.getClass();

        this.beanInfo = Introspector.getBeanInfo(type);
        this.id = findIdProperty();

        setOpenEngSBModelTail(new ArrayList<OpenEngSBModelEntry>());
    }

    private PropertyDescriptor findIdProperty() {
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            try {
                Field f = type.getField(pd.getName());
                if (f.isAnnotationPresent(OpenEngSBModelId.class)) {
                    return pd;
                }
            } catch (NoSuchFieldException e) {
            }

            if (pd.getReadMethod().isAnnotationPresent(OpenEngSBModelId.class)) {
                return pd;
            }
        }

        return null;
    }

    @Override
    public List<OpenEngSBModelEntry> toOpenEngSBModelValues() {
        List<OpenEngSBModelEntry> values = new ArrayList<>();

        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if ("class".equals(pd.getName())) {
                continue;
            }

            try {
                values.add(new OpenEngSBModelEntry(pd.getName(), pd.getReadMethod().invoke(object), pd
                    .getPropertyType()));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return values;
    }

    @Override
    public List<OpenEngSBModelEntry> toOpenEngSBModelEntries() {
        ArrayList<OpenEngSBModelEntry> list = new ArrayList<>();

        list.addAll(toOpenEngSBModelValues());
        list.addAll(getOpenEngSBModelTail());

        return list;
    }

    @Override
    public Object retrieveInternalModelId() {
        if (id == null) {
            return null;
        }

        try {
            return id.getReadMethod().invoke(object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public Long retrieveInternalModelTimestamp() {
        return getTailEntry("edbTimestamp");
    }

    @Override
    public Integer retrieveInternalModelVersion() {
        return getTailEntry("edbVersion");
    }

    @Override
    public void addOpenEngSBModelEntry(OpenEngSBModelEntry entry) {
        tail.add(entry);
    }

    @Override
    public void removeOpenEngSBModelEntry(String key) {
        Iterator<OpenEngSBModelEntry> iterator = tail.iterator();

        while (iterator.hasNext()) {
            if (key.equals(iterator.next().getKey())) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public List<OpenEngSBModelEntry> getOpenEngSBModelTail() {
        return tail;
    }

    @Override
    public void setOpenEngSBModelTail(List<OpenEngSBModelEntry> entries) {
        this.tail = entries;
    }

    @Override
    public String retrieveModelName() {
        return type.getName();
    }

    @Override
    public String retrieveModelVersion() {
        return "1.0.0"; // FIXME
    }

    @SuppressWarnings("unchecked")
    protected <T> T getTailEntry(String name) {
        for (OpenEngSBModelEntry entry : getOpenEngSBModelTail()) {
            if (name.equals(entry.getKey())) {
                return (T) entry.getValue();
            }
        }

        return null;
    }

}
