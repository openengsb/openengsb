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
package org.openengsb.config.service;

import java.util.Map;

import org.openengsb.config.domain.PersistedObject;

public interface EditorService {
    /**
     * Validates that a possible name update of the {@code PersistedObject} is
     * still unique for the {@code PersistedObjects}'s {@code ServiceAssembly}.
     */
    boolean validateNameUpdate(PersistedObject po, Map<String, String> updates);

    /**
     * Updates the given {@code PersistedObject} from the key-values pairs.
     */
    void updatePersistedObject(PersistedObject po, Map<String, String> updates);
}
