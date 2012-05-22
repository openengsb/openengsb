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

package org.openengsb.core.security.filter;

import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;

/**
 * extracts the original {@link org.openengsb.core.api.remote.MethodCallRequest} from a {@link SecureRequest} and
 * invokes the next filter. The resulting {@link MethodResultMessage} is then wrapped into a {@link SecureResponse}
 *
 *
 * <code>
 * <pre>
 *      [SecureRequest]  > Filter > [MethodCallRequest]     > ...
 *                                                             |
 *                                                             v
 *      [SecureResponse] < Filter < [MethodResultMessage]   < ...
 * </pre>
 * </code>
 */
public class WrapperFilter extends AbstractFilterChainElement<SecureRequest, SecureResponse> {

    private FilterAction next;

    @Override
    protected SecureResponse doFilter(SecureRequest input, Map<String, Object> metaData) {
        MethodResultMessage resultMessage = (MethodResultMessage) next.filter(input.getMessage(), metaData);
        return SecureResponse.create(resultMessage);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, MethodCallRequest.class, MethodResultMessage.class);
        this.next = next;
    }
}
