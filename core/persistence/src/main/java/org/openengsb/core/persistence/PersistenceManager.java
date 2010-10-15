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

package org.openengsb.core.persistence;

import org.osgi.framework.Bundle;

/**
 * 
 * The persistence manager is responsible for managing persistence service instances per bundle. If a bundle first calls
 * {@link #getPersistenceForBundle(Bundle)} then the persistence service for this bundle has to be created. Later calls
 * to this method from the same bundle should return exactly the same persistence service instance.
 */
public interface PersistenceManager {

    /**
     * Get a {@link PersistenceService} instance for the specified bundle. The symbolic name and version of the bundle
     * is used to create a unique persistence service for a bundle. This means that if this method is called twice with
     * the same bundle exactly the same persistence reference is returned.
     */
    PersistenceService getPersistenceForBundle(Bundle bundle);

}
