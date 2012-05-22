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

package org.openengsb.domain.auditing;

import java.util.List;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.security.annotation.Anonymous;

// @extract-start AuditingDomain
/**
 * The Auditing Domain provides means to audit certain events for later viewing.
 */
public interface AuditingDomain extends Domain {

    @Anonymous
    void onEvent(Event event);

    List<Event> getAllAudits();

    @Anonymous
    void onNodeStart(String process, long instance, String name);

    @Anonymous
    void onNodeFinish(String process, long instance, String name);

}
// @extract-end

