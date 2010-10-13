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

package org.openengsb.core.common.context;

import java.util.List;

/**
 * Service for setting the thread-local context for further query- and management-requests.
 */
public interface ContextCurrentService extends ContextService {
    /**
     * Sets the the thread-local context identified by the given {@code contextId}.
     *
     * @throws IllegalArgumentException if {@code contextId} specifies a non-existing context.
     */
    void setThreadLocalContext(String contextId);

    /**
     * Gets the thread-local context's {@code contextId}
     */
    String getThreadLocalContext();

    /**
     * Creates a new empty context with the given {@code contextId}.
     *
     * @throws IllegalArgumentException if a context with the given id exists
     */
    void createContext(String contextId);

    /**
     * Get all root context nodes in a natural order. These can be set using the {@link #setThreadLocalContext(String)}
     * method.
     */
    List<String> getAvailableContexts();

}
