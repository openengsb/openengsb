package org.openengsb.ui.common.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.web.FilterChainProxy;

public class DelegatingSecurityFilter implements Filter, ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingSecurityFilter.class);

    private ApplicationContext applicationContext;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {
        Filter delegatedFilter = getFilterBean();
        if (delegatedFilter != null) {
            LOGGER.debug("delegate to org.springframework.security.web.filterChainProxy");
            delegatedFilter.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    private Filter getFilterBean() {
        if (applicationContext == null) {
            throw new IllegalStateException("Could not load required security filter, because no application context was set.");
        }
        try {
            return applicationContext.getBean(FilterChainProxy.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("Could not load required security filter", e);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void destroy() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
