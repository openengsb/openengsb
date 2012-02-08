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
import org.openengsb.core.api.Raises;


// @extract-start ExampleDomain
/**
 * This is the interface of the example domain. Besides this functional interface, which has to be implemented by
 * connectors, this domain also provides the event interface {@link ExampleDomainEvents}, which can be used by
 * connectors.
 */
public interface ExampleDomain extends Domain {

    @Raises(LogEvent.class)
    String doSomething(String message);
    
    ExampleResponseModel doSomething(ExampleRequestModel model);

    String doSomething(ExampleEnum exampleEnum);

    String doSomethingWithLogEvent(LogEvent event);

    public enum ExampleEnum {
        ONE, TWO, THREE
    }
}
// @extract-end
