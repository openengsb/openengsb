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

package org.openengsb.core.api;

import org.openengsb.core.api.security.annotation.Anonymous;

/**
 * Every service published within the OpenEngSB context creates an instanceId, which identifies itself unique in the
 * system. Those unique values are also interested directly from the code to identify which OpenEngSB someone is working
 * with. The {@link #getInstanceId()} method returns exactly this unique id for each OpenEngSB service.
 */
public interface OpenEngSBService {

    /**
     * Each created service in the OpenEngSB has its own unique id which could be resolved from code by this method.
     */
    @Anonymous
    String getInstanceId();

}

