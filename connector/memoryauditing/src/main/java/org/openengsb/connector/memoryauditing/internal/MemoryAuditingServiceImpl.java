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

package org.openengsb.connector.memoryauditing.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openengsb.core.common.AliveState;
import org.openengsb.domain.auditing.AuditingDomain;

public class MemoryAuditingServiceImpl implements AuditingDomain {

    @SuppressWarnings("unchecked")
    private final List<String> messages = Collections.synchronizedList(new ArrayList<String>());

    public MemoryAuditingServiceImpl() {
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public void audit(String message) {
        messages.add(message);
    }

    @Override
    public List<String> getAudits() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public String getInstanceId() {
        return "auditing";
    }
}
