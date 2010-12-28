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

package org.openengsb.core.workflow.internal;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.workflow.EventRegistrationService;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.model.RemoteEvent;

public class RegistrationServiceImpl implements EventRegistrationService {

    private Log log = LogFactory.getLog(RegistrationServiceImpl.class);

    private RuleManager ruleManager;

    @Override
    public void registerEvent(RemoteEvent event, String portId, URI returnAddress) {
        log.info("registering Event: " + event);
    }

}
