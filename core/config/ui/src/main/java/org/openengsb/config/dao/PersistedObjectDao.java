/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.config.dao;

import java.util.List;

import org.openengsb.config.domain.PersistedObject;
import org.openengsb.config.domain.ServiceAssembly;

public interface PersistedObjectDao extends BaseDao<PersistedObject> {
    /**
     * Returns the {@code PersistedObject} with the unique name or null if no
     * {@code PersistedObject} with the given name exists.
     *
     * @param name the unique name of the endpoint
     */
    PersistedObject findByName(String name);

    /**
     * Returns a list of all {@code PersistedObject}s ordered by their name for
     * the given
     * 
     * @{code ServiceAssembly}.
     */
    List<PersistedObject> findByServiceAssembly(ServiceAssembly sa);

    /**
     * Returns the {@code PersistdObject} with the unique name or null if no
     * {@code PersistedObject} with the given name for the given {@code
     * ServiceAssembly} exists.
     */
    PersistedObject findByName(ServiceAssembly sa, String name);
}
