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

package org.openengsb.core.test;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * May be used when {@link org.mockito.stubbing.OngoingStubbing#thenReturn(Object)} does not work because of generic
 * type incompatibilities
 */
public class ValueAnswer<T> implements Answer<T> {
    private T answer;

    public ValueAnswer(T answer) {
        this.answer = answer;
    }

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
        return answer;
    }
}
