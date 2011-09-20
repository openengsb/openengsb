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

package org.openengsb.core.test;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.osgi.framework.Bundle;

public class DummyPersistenceManager implements PersistenceManager {

    Map<Bundle, PersistenceService> services = new HashMap<Bundle, PersistenceService>();

    @Override
    public PersistenceService getPersistenceForBundle(Bundle bundle) {
        if (!services.containsKey(bundle)) {
            services.put(bundle, new DummyPersistence());
        }
        return services.get(bundle);
    }

}
