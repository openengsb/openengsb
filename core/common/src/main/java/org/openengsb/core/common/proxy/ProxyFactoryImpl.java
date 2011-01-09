package org.openengsb.core.common.proxy;

import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.communication.CallRouter;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class ProxyFactoryImpl implements BundleContextAware, ProxyFactory {

    private CallRouter callRouter;
    private BundleContext bundleContext;

    @Override
    public ProxyServiceManager createProxyForDomain(DomainProvider provider) {
        ProxyServiceManager proxyServiceManager = new ProxyServiceManager(provider, callRouter);
        proxyServiceManager.setBundleContext(bundleContext);
        return proxyServiceManager;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setCallRouter(CallRouter callRouter) {
        this.callRouter = callRouter;
    }
}
