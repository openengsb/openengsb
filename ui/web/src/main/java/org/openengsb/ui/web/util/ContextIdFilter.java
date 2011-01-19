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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.ContextHolder;
import org.openengsb.core.common.context.ContextCurrentService;
import org.springframework.web.filter.GenericFilterBean;

public class ContextIdFilter extends GenericFilterBean {

    private Log log = LogFactory.getLog(ContextIdFilter.class);

    static final String FILTER_APPLIED = "__openengsb_context_filter_applied";
    static final String CONTEXT_ID_ATTRIBUTE_NAME = "__openengsb_context_id";
    private ContextCurrentService contextService;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        log.debug("checking if filter was already applied");
        if (request.getAttribute(FILTER_APPLIED) != null) {
            // ensure that filter is only applied once per request
            log.debug("it was, so moving on");
            chain.doFilter(request, response);
            return;
        }
        request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
        log.info("doing the context filter");
        HttpSession httpSession = request.getSession();

        String contextBeforeChainExecution = (String) httpSession.getAttribute(CONTEXT_ID_ATTRIBUTE_NAME);
        log.info("retrieved contextId " + contextBeforeChainExecution + " from session");
        try {
            String oldContext = ContextHolder.get().getCurrentContextId();
            log.info("old Context was " + oldContext);
            log.info("now setting " + contextBeforeChainExecution + "as threadlocal context");
            ContextHolder.get().setCurrentContextId(contextBeforeChainExecution);
            // SecurityContextHolder.setContext(contextBeforeChainExecution);
            chain.doFilter(req, res);
        } finally {
            log.info("request done; extracting threadlocal context... " + ContextHolder.get().getCurrentContextId());
            String contextAfterChainExecution = ContextHolder.get().getCurrentContextId();
            // leave it lying around in the threadlocal for now
            log.info("storing it into the session... done");
            httpSession.setAttribute(CONTEXT_ID_ATTRIBUTE_NAME, contextAfterChainExecution);
            request.removeAttribute(FILTER_APPLIED);
        }

    }

    public void setContextService(ContextCurrentService contextService) {
        this.contextService = contextService;
    }
}
