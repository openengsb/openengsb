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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.common.util.ModelUtils;

public class EKBProxyOutgoingFilterTest {

    private EKBProxyOutgoingFilter filter;

    @Before
    public void setup() {
        filter = new EKBProxyOutgoingFilter();
        FilterAction action = new TestFilter();
        filter.setNext(action);
    }

    @Test
    public void testConversionMethodCallWithOpenEngSBModelParameter_shouldWork() {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setId(10);
        model.setName("test");

        MethodCallMessage request = new MethodCallMessage();
        request.setCallId("outgoing1");
        MethodCall call = new MethodCall();
        call.setArgs(new Object[]{ model });
        request.setMethodCall(call);

        MethodResultMessage message = filter.doFilter(request, null);

        assertThat(message.getCallId(), is("success"));
        @SuppressWarnings("unchecked")
        List<String> elements = (List<String>) message.getResult().getArg();
        assertThat(elements.contains("id=10"), is(true));
        assertThat(elements.contains("name=test"), is(true));
    }

    @Test
    public void testConversionMethodCallWithOpenEngSBModelWrapperAnswer_shouldWork() {
        MethodCallMessage request = new MethodCallMessage();
        request.setCallId("outgoing2");
        request.setMethodCall(new MethodCall());
        MethodResultMessage message = filter.doFilter(request, null);

        assertThat(message.getCallId(), is("success"));
        assertThat(message.getResult().getArg(), instanceOf(TestModel.class));
        TestModel model = (TestModel) message.getResult().getArg();
        assertThat(model.getId(), is(100));
        assertThat(model.getName(), is("test"));
    }

    @Test
    public void testBothConversionSteps_shouldWork() {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setId(70);
        model.setName("test");

        MethodCallMessage request = new MethodCallMessage();
        request.setCallId("outgoing3");
        MethodCall call = new MethodCall();
        call.setArgs(new Object[]{ model });
        request.setMethodCall(call);

        MethodResultMessage message = filter.doFilter(request, null);

        assertThat(message.getCallId(), is("success"));
        TestModel ret = (TestModel) message.getResult().getArg();
        assertThat(ret.getId(), is(70));
        assertThat(ret.getName(), is("test"));
    }
}
