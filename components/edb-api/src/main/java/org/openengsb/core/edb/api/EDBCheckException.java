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

package org.openengsb.core.edb.api;

import java.util.List;

/**
* This exception class is a detail EDB exception. It is thrown if during the check of a commit, there are
* failing elements in the commit. The failing elements are listed in this exception.
*/
@SuppressWarnings("serial")
public class EDBCheckException extends EDBException {
    private List<EDBObject> failedInserts;
    private List<EDBObject> failedUpdates;
    private List<String> failedDeletes;
    
    public EDBCheckException() {
        super();
    }

    public EDBCheckException(String s) {
        super(s);
    }

    public EDBCheckException(Throwable t) {
        super(t);
    }

    public EDBCheckException(String s, Throwable t) {
        super(s, t);
    }

    public List<EDBObject> getFailedInserts() {
        return failedInserts;
    }

    public void setFailedInserts(List<EDBObject> failedInserts) {
        this.failedInserts = failedInserts;
    }

    public List<EDBObject> getFailedUpdates() {
        return failedUpdates;
    }

    public void setFailedUpdates(List<EDBObject> failedUpdates) {
        this.failedUpdates = failedUpdates;
    }

    public List<String> getFailedDeletes() {
        return failedDeletes;
    }

    public void setFailedDeletes(List<String> failedDeletes) {
        this.failedDeletes = failedDeletes;
    }
}
