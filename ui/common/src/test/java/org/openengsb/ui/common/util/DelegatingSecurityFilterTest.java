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

package org.openengsb.ui.common.util;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.wicket.api.ConfigurableFilterConfig;
import org.osgi.framework.BundleContext;

public class DelegatingSecurityFilterTest {

    private DelegatingSecurityFilterFactory filterFactory;
    private Filter delegateFilter;
    
    @Before
    public void setUp() {
        filterFactory = new DelegatingSecurityFilterFactory(mock(BundleContext.class), "appName", 1);
        delegateFilter = mock(Filter.class);
        filterFactory.setSecurityFilterChain(delegateFilter);
    }
    
    @Test
    public void testCreateFilter_shouldReturnDelegatingSecurityFilter() {
        assertThat(filterFactory.createFilter(mock(ConfigurableFilterConfig.class)),
            CoreMatchers.instanceOf(DelegatingSecurityFilter.class));
    }
    
    @Test
    public void testDoFilter_shouldDelegateToSecurityFilterChain() throws Exception {
        ServletRequest req = mock(ServletRequest.class);
        ServletResponse resp = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        Filter filter = filterFactory.createFilter(mock(ConfigurableFilterConfig.class));
        
        filter.doFilter(req, resp, chain);
        
        verify(delegateFilter).doFilter(req, resp, chain);
    }
    
    @Test(expected = IllegalStateException.class)
    public void testDoFilterWithoutSettedDelegatee_shouldThrowException() throws Exception {
        ServletRequest req = mock(ServletRequest.class);
        ServletResponse resp = mock(ServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        DelegatingSecurityFilter filter = new DelegatingSecurityFilter();
        
        filter.doFilter(req, resp, chain);
    }
}
