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

package org.openengsb.domain.example;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.EventSupport;
import org.openengsb.core.api.Raises;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;


// @extract-start ExampleDomain
/**
 * This is the interface of the example domain. Besides this functional interface, which has to be implemented by
 * connectors, this domain also provides the event interface {@link ExampleDomainEvents}, which can be used by
 * connectors.
 */
@SecurityAttribute("domain.example")
public interface ExampleDomain extends Domain {

    @SecurityAttribute("something")
    @Raises(LogEvent.class)
    String doSomethingWithMessage(String message);

    ExampleResponseModel doSomethingWithModel(ExampleRequestModel model);

    @SecurityAttribute("event")
    String doSomethingWithLogEvent(LogEvent event);

    public enum ExampleEnum {
        ONE, TWO, THREE
    }
}
// @extract-end
