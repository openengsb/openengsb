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

package org.openengsb.core.ekb.api;

import java.util.List;

/**
 * This exception class is thrown in the PersistInterface whenever a problem during the model persist procedure has been
 * registered at EDB Level. It contains a list for every persisting type (insert/update/delete) with the failed model
 * object identifiers.
 */
@SuppressWarnings("serial")
public class ModelPersistException extends EKBException {
    private List<String> failedInserts;
    private List<String> failedUpdates;
    private List<String> failedDeletions;

    public ModelPersistException(List<String> failedInserts, List<String> failedUpdates,
            List<String> failedDeletions, Exception e) {
        super("Registered a potential conflict during the EDB persisting.", e);
        this.failedInserts = failedInserts;
        this.failedUpdates = failedUpdates;
        this.failedDeletions = failedDeletions;
    }

    public List<String> getFailedInserts() {
        return failedInserts;
    }

    public List<String> getFailedUpdates() {
        return failedUpdates;
    }

    public List<String> getFailedDeletions() {
        return failedDeletions;
    }

}
