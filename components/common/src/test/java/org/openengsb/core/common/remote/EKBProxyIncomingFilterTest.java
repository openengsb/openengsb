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


public class EKBProxyIncomingFilterTest {

//    private EKBProxyIncomingFilter filter;
//
//    @Before
//    public void setup() {
//        filter = new EKBProxyIncomingFilter();
//        FilterAction action = new TestFilter();
//        filter.setNext(action);
//    }
//
//    @Test
//    public void testConversionMethodCallWithOpenEngSBModelWrapperParameter_shouldWork() {
//        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
//        entries.add(new OpenEngSBModelEntry("id", 50, Integer.class));
//        entries.add(new OpenEngSBModelEntry("name", "test", String.class));
//        OpenEngSBModelWrapper wrapper = new OpenEngSBModelWrapper();
//        wrapper.setModelClass(TestModel.class.getName());
//        wrapper.setEntries(entries);
//
//        MethodCallRequest request = new MethodCallRequest();
//        request.setCallId("incoming1");
//        MethodCall call = new MethodCall();
//        call.setArgs(new Object[]{ wrapper });
//        request.setMethodCall(call);
//
//        MethodResultMessage message = filter.doFilter(request, null);
//
//        assertThat(message.getCallId(), is("success"));
//        @SuppressWarnings("unchecked")
//        List<String> elements = (List<String>) message.getResult().getArg();
//        assertThat(elements.contains("id=50"), is(true));
//        assertThat(elements.contains("name=test"), is(true));
//    }
//
//    @Test
//    public void testConversionMethodCallWithOpenEngSBModelAnswer_shouldWork() {
//        MethodCallRequest request = new MethodCallRequest();
//        request.setCallId("incoming2");
//        request.setMethodCall(new MethodCall());
//        MethodResultMessage message = filter.doFilter(request, null);
//
//        assertThat(message.getCallId(), is("success"));
//        assertThat(message.getResult().getArg(), instanceOf(OpenEngSBModelWrapper.class));
//        OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) message.getResult().getArg();
//
//        Integer id = 0;
//        String name = "";
//
//        for (OpenEngSBModelEntry entry : wrapper.getEntries()) {
//            if (entry.getKey().equals("id")) {
//                id = (Integer) entry.getValue();
//            }
//            if (entry.getKey().equals("name")) {
//                name = (String) entry.getValue();
//            }
//        }
//
//        assertThat(id, is(60));
//        assertThat(name, is("test"));
//    }
//
//    @Test
//    public void testBothConversionSteps_shouldWork() {
//        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
//        entries.add(new OpenEngSBModelEntry("id", 50, Integer.class));
//        entries.add(new OpenEngSBModelEntry("name", "test", String.class));
//        OpenEngSBModelWrapper wrapper = new OpenEngSBModelWrapper();
//        wrapper.setModelClass(TestModel.class.getName());
//        wrapper.setEntries(entries);
//
//        MethodCallRequest request = new MethodCallRequest();
//        request.setCallId("incoming3");
//        MethodCall call = new MethodCall();
//        call.setArgs(new Object[]{ wrapper });
//        request.setMethodCall(call);
//
//        MethodResultMessage message = filter.doFilter(request, null);
//
//        assertThat(message.getCallId(), is("success"));
//        OpenEngSBModelWrapper result = (OpenEngSBModelWrapper) message.getResult().getArg();
//
//        Integer id = 0;
//        String name = "";
//
//        for (OpenEngSBModelEntry entry : result.getEntries()) {
//            if (entry.getKey().equals("id")) {
//                id = (Integer) entry.getValue();
//            }
//            if (entry.getKey().equals("name")) {
//                name = (String) entry.getValue();
//            }
//        }
//
//        // values get changed in the TestFilter class
//        assertThat(wrapper.getModelClass(), is(TestModel.class.getName()));
//        assertThat(id, is(60));
//        assertThat(name, is("teststring"));
//    }
}
