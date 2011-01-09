package org.openengsb.core.common.proxy;

import org.openengsb.core.common.DomainProvider;

public interface ProxyFactory {
    ProxyServiceManager createProxyForDomain(DomainProvider provider);
}
