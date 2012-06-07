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

package org.openengsb.connector.memoryauditing.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Event;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.auditing.AuditingDomain;

import com.google.common.collect.Maps;

public class MemoryAuditingServiceImpl extends AbstractOpenEngSBConnectorService implements AuditingDomain {

    private final List<Event> events = Collections.synchronizedList(new ArrayList<Event>());
    private final ConcurrentMap<Long, List<String>> workflowProgress = Maps.newConcurrentMap();

    public MemoryAuditingServiceImpl() {
    }

    public MemoryAuditingServiceImpl(String instanceId) {
        super(instanceId);
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public void onEvent(Event event) {
        events.add(event);
    }

    @Override
    public List<Event> getAllAudits() {
        return Collections.unmodifiableList(events);
    }

    @Override
    public void onNodeStart(String process, long instance, String name) {
        workflowProgress.putIfAbsent(instance, new LinkedList<String>());
        workflowProgress.get(instance).add(name);
    }

    @Override
    public void onNodeFinish(String process, long instance, String name) {
        // do nothing
    }

}
