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

package org.openengsb.core.common.util;

import org.openengsb.core.api.context.ContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * keeps track of thread-locals important to the OpenEngSB context-aware threads can use this to initialize the context
 * properly in a new (or used) thread
 */
abstract class ContextAware {

    private String context = ContextHolder.get().getCurrentContextId();
    private SecurityContext securityContext = SecurityContextHolder.getContext();

    protected void applyContext() {
        ContextHolder.get().setCurrentContextId(context);
        SecurityContextHolder.setContext(securityContext);
    }

}
