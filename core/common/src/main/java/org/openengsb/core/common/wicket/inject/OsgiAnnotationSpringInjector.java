package org.openengsb.core.common.wicket.inject;

import org.apache.wicket.injection.ConfigurableInjector;
import org.apache.wicket.injection.IFieldValueFactory;

public class OsgiAnnotationSpringInjector extends ConfigurableInjector {

    private IFieldValueFactory factory;

    public OsgiAnnotationSpringInjector(OsgiSpringBeanReceiverLocator springBeanReceiverLocator) {
        factory = new OsgiAnnotationProxyFieldValueFactory(springBeanReceiverLocator);
    }

    @Override
    protected IFieldValueFactory getFieldValueFactory() {
        return factory;
    }

}
