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
 * {@link FilterAction}s can always be used as last element of a filter-chain. {@link FilterAction} that are designed to
 * use other filters to calculate their result, must implement this interface. This way the filters can be exchanged
 * easily if they support the same Input and Output types.
 *
 * A separate interface is used because the last element in a filterchain would not need a setNext-method
 *
 * {@link FilterChainElement}s should never be instantiated directly. A {@link FilterChainElementFactory} should be used
 * for that purpose.
 */
public interface FilterChainElement extends FilterAction {

    /**
     * used to integrate the filter in the chain. The implementation of this method should first check if the given
     * filter is compatible and throw a {@link FilterConfigurationException} if the types do not match.
     *
     * @throws FilterConfigurationException if the {@link FilterAction}s are not compatible
     */
    void setNext(FilterAction next) throws FilterConfigurationException;

}
