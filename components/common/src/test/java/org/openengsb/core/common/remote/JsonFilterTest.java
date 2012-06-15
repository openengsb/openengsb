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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.internal.stubbing.answers.Returns;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResultMessage;

public class JsonFilterTest {

    private static final String MESSAGE = ""
            + "{"
            + "   \"result\":{"
            + "      \"type\":\"Object\","
            + "      \"arg\":{"
            + "         \"id\":\"42\","
            + "         \"name\":\"foo\""
            + "      },"
            + "      \"metaData\":{"
            + ""
            + "      },"
            + "      \"className\":\"org.openengsb.core.common.remote.TestModel\""
            + "   },"
            + "   \"timestamp\":634752977414591025,"
            + "   \"callId\":\"22b3007f-c9b0-4f4a-b1b8-6538499983a3\""
            + "}";

    @Test
    public void methodCallMarashalFilter() throws Exception {
        JsonOutgoingMethodCallMarshalFilter jsonOutgoingMethodCallMarshalFilter =
            new JsonOutgoingMethodCallMarshalFilter();
        FilterAction mock = mock(FilterAction.class);
        when(mock.filter(any(MethodCallMessage.class), any(Map.class))).thenReturn(MESSAGE);
        when(mock.getSupportedInputType()).thenAnswer(new Returns(String.class));
        when(mock.getSupportedOutputType()).thenAnswer(new Returns(String.class));
        jsonOutgoingMethodCallMarshalFilter.setNext(mock);
        MethodResultMessage result =
            (MethodResultMessage) jsonOutgoingMethodCallMarshalFilter.filter(new MethodCallMessage(),
                new HashMap<String, Object>());
        Object arg = result.getResult().getArg();
        assertThat(arg, is(TestModel.class));
        TestModel resultModel = (TestModel) arg;
        assertThat(resultModel.getId(), is(42));
        assertThat(resultModel.getName(), is("foo"));
    }
}
