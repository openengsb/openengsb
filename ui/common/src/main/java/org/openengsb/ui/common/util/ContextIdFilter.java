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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openengsb.core.api.context.ContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextIdFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextIdFilter.class);

    static final String FILTER_APPLIED = "__openengsb_context_filter_applied";
    static final String CONTEXT_ID_ATTRIBUTE_NAME = "__openengsb_context_id";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
        ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (request.getAttribute(FILTER_APPLIED) != null) {
            LOGGER.debug("filter was already applied, moving on to next Filter in chain");
            chain.doFilter(request, response);
            return;
        }
        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
        LOGGER.debug("applying Context-id-filter");
        HttpSession httpSession = request.getSession();

        String contextBeforeChainExecution = (String) httpSession.getAttribute(CONTEXT_ID_ATTRIBUTE_NAME);
        try {
            if (LOGGER.isDebugEnabled()) {
                String oldContext = ContextHolder.get().getCurrentContextId();
                if (oldContext == null || !oldContext.equals(contextBeforeChainExecution)) {
                    LOGGER.debug("correcting threadlocal context-id from {} to {} in thread {}", 
                        new Object[] { oldContext, contextBeforeChainExecution, Thread.currentThread().getId() });
                }
            }
            ContextHolder.get().setCurrentContextId(contextBeforeChainExecution);
            chain.doFilter(req, res);
        } finally {
            String currentContextId = ContextHolder.get().getCurrentContextId();
            LOGGER.debug("request done; storing threadlocal context  to session ({})", currentContextId);
            String contextAfterChainExecution = currentContextId;
            // leave it lying around in the threadlocal for now
            httpSession.setAttribute(CONTEXT_ID_ATTRIBUTE_NAME, contextAfterChainExecution);
            request.removeAttribute(FILTER_APPLIED);
        }

    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}
