package org.openengsb.core.common.composite;

import java.lang.reflect.Method;
import java.util.List;

import org.openengsb.core.api.CompositeConnectorStrategy;
import org.osgi.framework.ServiceReference;

public abstract class AbstractDelegateStrategy implements CompositeConnectorStrategy {

    @Override
    public Object invoke(List<ServiceReference> services, Method method, Object... args) throws Throwable {
        return method.invoke(createDelegate(services), args);
    }

    protected abstract Object createDelegate(List<ServiceReference> services);

}
