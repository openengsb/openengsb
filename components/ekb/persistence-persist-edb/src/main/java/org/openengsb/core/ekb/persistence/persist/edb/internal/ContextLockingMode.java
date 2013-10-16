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

package org.openengsb.core.ekb.persistence.persist.edb.internal;

/**
 * The ContextLockingMode enumeration defines the possible modes of the context locking mechanism.
 * CONCURRENT_WRITE_PROTECTION = It is not possible that two processes write concurrently into the same context.
 * EXPECTED_HEAD_REVISION_CHECK = If it is activated, the additional "parent" parameter of the PersistInterface 
 * methods is considered. If the current head of a specific context is not the passed "parent" context,
 * an EKBConcurrentException is thrown.
 * BOTH = Both features are activated.
 * DEACTIVATED = Both features are deactivated.
 */
public enum ContextLockingMode {
    CONCURRENT_WRITE_PROTECTION,
    EXPECTED_HEAD_REVISION_CHECK,
    BOTH,
    DEACTIVATED;
    
    /**
     * Returns true if the concurrent write protection is activated and false otherwise.
     */
    public boolean isConcurrentWriteProtectionActivated() {
        switch (this) {
            case CONCURRENT_WRITE_PROTECTION:
            case BOTH:
                return true;
            case EXPECTED_HEAD_REVISION_CHECK:
            case DEACTIVATED:
            default:
        }
        return false;
    }
    
    /**
    * Returns true if the expected head revision check is activated and false otherwise.
    */
    public boolean isExpectedHeadRevisionCheckActivated() {
        switch (this) {
            case EXPECTED_HEAD_REVISION_CHECK:
            case BOTH:
                return true;
            case CONCURRENT_WRITE_PROTECTION:
            case DEACTIVATED:
            default:
        }
        return false;
    }
}
