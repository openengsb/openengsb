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

/**
 * Service for retrieving the root context for the current thread. This service provides a few convenience methods for
 * managing the {@code Context}.
 *
 * Path elements are separated by a slash ('/'), e.g. {@code /a/b/c} could mean a path that identifies a context child
 * {@code 'c'} three levels down or (in path and key combinations} a key {@code 'c'} two level down.
 */
public interface ContextService {

    /**
     * Sets the value for the given path and key parameter. This creates any needed sub contexts if they are not
     * existing.
     */
    void putValue(String pathAndKey, String value);

    /**
     * Returns the value for the given parameter or null if the given path and key does not exist.
     */
    String getValue(String pathAndKey);

    Context getContext(String path);

    /**
     * Returns the root @{code Context} for the current executing thread.
     */
    Context getContext();

    /**
     *
     * @return the id of the current context
     */
    String getCurrentContextId();
}
