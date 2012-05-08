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

package org.openengsb.core.common.remote;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterConfigurationException;

/**
 * Abstract Baseclass that aids when implementing new {@link FilterChainElement}s. This class is supposed to be used for
 * implementing {@link FilterAction}s which use other filters (next) for calculating their result.
 *
 */
public abstract class AbstractFilterChainElement<InputType, OutputType> extends
        AbstractFilterAction<InputType, OutputType>
        implements FilterChainElement {

    public AbstractFilterChainElement() {
    }

    protected AbstractFilterChainElement(Class<InputType> inputType, Class<OutputType> outputType) {
        super(inputType, outputType);
    }

    protected static final void checkNextInputAndOutputTypes(FilterAction next,
            Class<?> inputType, Class<?> outputType) throws FilterConfigurationException {
        if (!next.getSupportedInputType().isAssignableFrom(inputType)) {
            throw new FilterConfigurationException(String.format("inputTypes are not compatible %s (%s - %s)",
                next, next.getSupportedInputType(), inputType));
        }
        if (!next.getSupportedOutputType().isAssignableFrom(outputType)) {
            throw new FilterConfigurationException(String.format("outputTypes are not compatible (%s - %s)",
                next.getSupportedOutputType(), outputType));
        }
    }
}
