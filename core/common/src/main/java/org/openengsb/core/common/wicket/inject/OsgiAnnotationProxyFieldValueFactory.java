package org.openengsb.core.common.wicket.inject;

import java.lang.reflect.Field;

import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.proxy.IProxyTargetLocator;
import org.apache.wicket.proxy.LazyInitProxyFactory;

public class OsgiAnnotationProxyFieldValueFactory implements IFieldValueFactory {

    private OsgiSpringBeanReceiverLocator springBeanReceiverLocator;

    public OsgiAnnotationProxyFieldValueFactory(OsgiSpringBeanReceiverLocator springBeanReceiverLocator) {
        this.springBeanReceiverLocator = springBeanReceiverLocator;
    }

    @Override
    public Object getFieldValue(Field field, Object fieldOwner) {
        if (!field.isAnnotationPresent(OsgiSpringBean.class)) {
            return null;
        }
        String springBeanName = field.getAnnotation(OsgiSpringBean.class).springBeanName();
        String bundleSymbolicName = field.getAnnotation(OsgiSpringBean.class).bundleSymbolicName();
        return LazyInitProxyFactory.createProxy(field.getType(), new ProxyTargetLocator(springBeanName,
                    bundleSymbolicName, springBeanReceiverLocator));
    }

    @Override
    public boolean supportsField(Field field) {
        return field.isAnnotationPresent(OsgiSpringBean.class);
    }

    @SuppressWarnings("serial")
    private static class ProxyTargetLocator implements IProxyTargetLocator {
        private String springBeanName;
        private String bundleSymbolicName;
        private OsgiSpringBeanReceiverLocator locator;

        public ProxyTargetLocator(String springBeanName, String bundleSymbolicName,
                OsgiSpringBeanReceiverLocator locator) {
            this.springBeanName = springBeanName;
            this.bundleSymbolicName = bundleSymbolicName;
            this.locator = locator;
        }

        @Override
        public Object locateProxyTarget() {
            return locator.getSpringBeanReceiver().getBean(springBeanName, bundleSymbolicName);
        }
    }
}

