/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.web.util;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.context.ContextHolder;

public class ContextIdFilter implements Filter {

    private Log log = LogFactory.getLog(ContextIdFilter.class);

    static final String FILTER_APPLIED = "__openengsb_context_filter_applied";
    static final String CONTEXT_ID_ATTRIBUTE_NAME = "__openengsb_context_id";

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
        ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (request.getAttribute(FILTER_APPLIED) != null) {
            log.debug("filter was already applied, moving on to next Filter in chain");
            chain.doFilter(request, response);
            return;
        }
        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
        log.debug("applying Context-id-filter");
        HttpSession httpSession = request.getSession();

        String contextBeforeChainExecution = (String) httpSession.getAttribute(CONTEXT_ID_ATTRIBUTE_NAME);
        try {
            if (log.isDebugEnabled()) {
                String oldContext = ContextHolder.get().getCurrentContextId();
                if (oldContext == null || !oldContext.equals(contextBeforeChainExecution)) {
                    log.debug(String.format("correcting threadlocal context-id from %s to %s in thread %s", oldContext,
                        contextBeforeChainExecution, Thread.currentThread().getId()));
                }
            }
            ContextHolder.get().setCurrentContextId(contextBeforeChainExecution);
            chain.doFilter(req, res);
        } finally {
            String currentContextId = ContextHolder.get().getCurrentContextId();
            log.debug(String.format("request done; storing threadlocal context  to session (%s)", currentContextId));
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
