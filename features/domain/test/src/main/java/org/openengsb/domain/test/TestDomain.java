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

package org.openengsb.domain.test;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.Raises;

/**
 * This domain can be used to run tests for projects. The affected project is usually configured in the respective tool
 * connector.
 */
public interface TestDomain extends Domain {

    /**
     * run all tests for the currently configured project. This method returns at once with an id. The tests are run
     * asynchronously. The result can be retrieved using the events raised by this domain, which also contain the id.
     */
    @Raises({ TestStartEvent.class, TestSuccessEvent.class, TestFailEvent.class })
    String runTests();

    /**
     * run all tests for the currently configured project. This method returns at once with an id. The tests are run
     * asynchronously. The result can be retrieved using the events raised by this domain, with the processId set to the
     * supplied processId
     */
    @Raises({ TestStartEvent.class, TestSuccessEvent.class, TestFailEvent.class })
    void runTests(long processId);
}
