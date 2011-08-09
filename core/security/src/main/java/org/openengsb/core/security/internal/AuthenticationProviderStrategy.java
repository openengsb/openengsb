package org.openengsb.core.security.internal;

import java.util.Iterator;
import java.util.List;

import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.composite.AbstractDelegateStrategy;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class AuthenticationProviderStrategy extends AbstractDelegateStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationProviderStrategy.class);

    private static class CompositeAuthenticationProvider implements AuthenticationProvider {
        private List<ServiceReference> providers;

        public CompositeAuthenticationProvider(List<ServiceReference> providers) {
            this.providers = providers;
        }

        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            Iterator<AuthenticationProvider> serviceIterator =
                OpenEngSBCoreServices.getServiceUtilsService().getServiceIterator(providers,
                    AuthenticationProvider.class);
            AuthenticationException lastException = null;
            LOGGER.debug("iterating {} authenticationProviderServices", providers.size());
            while (serviceIterator.hasNext()) {
                AuthenticationProvider provider = serviceIterator.next();
                if (provider.supports(authentication.getClass())) {
                    LOGGER.info("attempting authentication using provider {}", provider.getClass());
                    try {
                        return provider.authenticate(authentication);
                    } catch (AuthenticationException e) {
                        lastException = e;
                    }
                }
            }

            if (lastException == null) {
                lastException =
                    new ProviderNotFoundException("No AuthenticationProvider found, that supports "
                            + authentication.getClass());
            }
            throw lastException;
        }

        @Override
        public boolean supports(final Class<? extends Object> authentication) {
            Iterator<AuthenticationProvider> serviceIterator =
                OpenEngSBCoreServices.getServiceUtilsService().getServiceIterator(providers,
                    AuthenticationProvider.class);
            return Iterators.any(serviceIterator, new Predicate<AuthenticationProvider>() {
                @Override
                public boolean apply(AuthenticationProvider input) {
                    return input.supports(authentication);
                }
            });
        }

    }

    @Override
    protected Object createDelegate(List<ServiceReference> services) {
        return new CompositeAuthenticationProvider(services);
    }

    @Override
    public boolean supports(Class<?> domainClass) {
        return AuthenticationProvider.class.isAssignableFrom(domainClass);
    }

}
