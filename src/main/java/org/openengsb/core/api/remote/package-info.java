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

package org.openengsb.core.api.remote;

/**
 * Package containing the interface definitions for executing remote services. The
 * {@link org.openengsb.core.common.communication.CallRouter} and
 * {@link org.openengsb.core.common.communication.RequestHandler} are required for delegating calls to different
 * ports implementations and receiving data from them. {@link org.openengsb.core.common.communication.MethodCall} and
 * {@link org.openengsb.core.common.communication.MethodReturn} describe the methods to call and their return values
 * form remote calls. The {@link org.openengsb.core.common.communication.OutgoingPort} interfaces have to be implemented
 * by ports to provide the logic used by the {@link org.openengsb.core.common.communication.CallRouter} and the
 * {@link org.openengsb.core.common.communication.RequestHandler}
 */
