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

import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;

import com.google.common.base.Preconditions;

/**
 * Abstract Baseclass that aids when implementing new {@link FilterAction}s. If the implemented filter is designed to
 * use other filters to calculate the result, the {@link AbstractFilterChainElement}-class should be used.
 */
public abstract class AbstractFilterAction<InputType, OutputType> implements FilterAction {

    private Class<InputType> inputType;
    private Class<OutputType> outputType;

    protected AbstractFilterAction(Class<InputType> inputType, Class<OutputType> outputType) {
        this.inputType = inputType;
        this.outputType = outputType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object filter(Object input, Map<String, Object> metaData) throws FilterException {
        Preconditions.checkArgument(inputType.isAssignableFrom(input.getClass()));
        return doFilter((InputType) input, metaData);
    }

    protected abstract OutputType doFilter(InputType input, Map<String, Object> metaData);

    @Override
    public Class<InputType> getSupportedInputType() {
        return inputType;
    }

    @Override
    public Class<OutputType> getSupportedOutputType() {
        return outputType;
    }

}
