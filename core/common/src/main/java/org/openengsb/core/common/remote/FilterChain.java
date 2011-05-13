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

/**
 * represents a chain of filters. This class basically represents just a FilterAction and can be used as final action of
 * another FilterChain.
 */
public class FilterChain implements FilterAction {
    private FilterAction firstElement;

    FilterChain(FilterAction firstElement) {
        this.firstElement = firstElement;
    }

    @Override
    public Object filter(Object input, Map<String, Object> metaData) throws FilterException {
        return firstElement.filter(input, metaData);
    }

    @Override
    public Class<?> getSupportedInputType() {
        return Object.class;
    }

    @Override
    public Class<?> getSupportedOutputType() {
        return Object.class;
    }
}
