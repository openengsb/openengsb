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

package org.openengsb.domains.example.connector.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.domains.example.ExampleDomainEvents;
import org.openengsb.domains.example.event.LogEvent;
import org.openengsb.domains.example.event.LogEvent.Level;

public class LogService implements ExampleDomain {

    private final Log log = LogFactory.getLog(getClass());
    private String outputMode;
    private final String id;
    private AliveState aliveState = AliveState.OFFLINE;
    private ExampleDomainEvents domainEventInterface;

    public LogService(String id, ExampleDomainEvents domainEventInterface) {
        this.id = id;
        this.domainEventInterface = domainEventInterface;
        aliveState = AliveState.CONNECTING;
    }

    @Override
    public String doSomething(String message) {
        message = id + ": " + message;
        Level level = Level.INFO;
        if ("DEBUG".equals(outputMode)) {
            log.debug(message);
            level = Level.DEBUG;
        } else if ("INFO".equals(outputMode)) {
            log.info(message);
            level = Level.INFO;
        } else if ("WARN".equals(outputMode)) {
            log.warn(message);
            level = Level.WARN;
        } else if ("ERROR".equals(outputMode)) {
            log.error(message);
            level = Level.ERROR;
        }
        raiseEvent(message, level);
        return "LogServiceCalled with: " + message;
    }

    private void raiseEvent(String message, Level level) {
        LogEvent event = new LogEvent();
        event.setMessage(message);
        event.setLevel(level);
        domainEventInterface.raiseEvent(event);
    }

    public void setOutputMode(String outputMode) {
        this.outputMode = outputMode;
        this.aliveState = AliveState.ONLINE;
    }

    @Override
    public AliveState getAliveState() {
        return this.aliveState;
    }
}
