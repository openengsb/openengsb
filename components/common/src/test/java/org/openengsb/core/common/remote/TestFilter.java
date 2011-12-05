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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelWrapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.common.util.ModelUtils;

public class TestFilter implements FilterAction {

    @Override
    public Object filter(Object input, Map<String, Object> metaData) throws FilterException {
        MethodCallRequest request = (MethodCallRequest) input;
        MethodResultMessage message = new MethodResultMessage();

        if (request.getCallId().equals("outgoing1")) {
            message = createOutputForOutgoing1(request);
        } else if (request.getCallId().equals("outgoing2")) {
            message = createOutputForOutgoing2(request);
        } else if (request.getCallId().equals("outgoing3")) {
            message = createOutputForOutgoing3(request);
        } else if (request.getCallId().equals("incoming1")) {
            message = createOutputForIncoming1(request);
        } else if (request.getCallId().equals("incoming2")) {
            message = createOutputForIncoming2(request);
        } else if (request.getCallId().equals("incoming3")) {
            message = createOutputForIncoming3(request);
        }

        if (message.getCallId() == null) {
            message.setCallId("failure");
        }
        return message;
    }

    private MethodResultMessage createOutputForOutgoing1(MethodCallRequest request) {
        MethodResultMessage message = new MethodResultMessage();
        Object arg = request.getMethodCall().getArgs()[0];
        if (arg.getClass().equals(OpenEngSBModelWrapper.class)) {
            List<String> result = new ArrayList<String>();
            OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) arg;
            result.add("class=" + wrapper.getClass().getName());
            for (OpenEngSBModelEntry entry : wrapper.getEntries()) {
                result.add(entry.getKey() + "=" + entry.getValue());
            }
            MethodResult r = new MethodResult();
            r.setArg(result);
            message.setCallId("success");
            message.setResult(r);
        }
        return message;
    }

    private MethodResultMessage createOutputForOutgoing2(MethodCallRequest request) {
        MethodResultMessage message = new MethodResultMessage();
        OpenEngSBModelWrapper wrapper = new OpenEngSBModelWrapper();
        wrapper.setModelClass(TestModel.class.getName());
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        entries.add(new OpenEngSBModelEntry("id", 100, Integer.class));
        entries.add(new OpenEngSBModelEntry("name", "test", String.class));
        wrapper.setEntries(entries);
        MethodResult r = new MethodResult();
        r.setArg(wrapper);
        message.setCallId("success");
        message.setResult(r);
        return message;
    }

    private MethodResultMessage createOutputForOutgoing3(MethodCallRequest request) {
        MethodResultMessage message = new MethodResultMessage();
        Object arg = request.getMethodCall().getArgs()[0];
        if (arg.getClass().equals(OpenEngSBModelWrapper.class)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                String  wrapperString = mapper.writeValueAsString(arg);
                OpenEngSBModelWrapper wrapper =
                    (OpenEngSBModelWrapper) mapper.readValue(wrapperString, OpenEngSBModelWrapper.class);
                MethodResult r = new MethodResult();
                r.setArg(wrapper);
                message.setCallId("success");
                message.setResult(r);
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return message;
    }

    private MethodResultMessage createOutputForIncoming1(MethodCallRequest request) {
        MethodResultMessage message = new MethodResultMessage();
        Object arg = request.getMethodCall().getArgs()[0];
        if (OpenEngSBModel.class.isAssignableFrom(arg.getClass())) {
            List<String> result = new ArrayList<String>();
            OpenEngSBModel model = (OpenEngSBModel) arg;
            for (OpenEngSBModelEntry entry : model.getOpenEngSBModelEntries()) {
                result.add(entry.getKey() + "=" + entry.getValue());
            }
            MethodResult r = new MethodResult();
            r.setArg(result);
            message.setCallId("success");
            message.setResult(r);
        }
        return message;
    }

    private MethodResultMessage createOutputForIncoming2(MethodCallRequest request) {
        MethodResultMessage message = new MethodResultMessage();
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setId(60);
        model.setName("test");
        MethodResult r = new MethodResult();
        r.setArg(model);
        message.setCallId("success");
        message.setResult(r);
        return message;
    }
    
    private MethodResultMessage createOutputForIncoming3(MethodCallRequest request) {
        MethodResultMessage message = new MethodResultMessage();
        Object arg = request.getMethodCall().getArgs()[0];
        if (TestModel.class.isAssignableFrom(arg.getClass())) {
            TestModel model = (TestModel) arg;
            model.setId(60);
            model.setName("teststring");
            MethodResult r = new MethodResult();
            r.setArg(model);
            message.setCallId("success");
            message.setResult(r);
        }
        return message;
    }

    @Override
    public Class<?> getSupportedInputType() {
        return MethodCallRequest.class;
    }

    @Override
    public Class<?> getSupportedOutputType() {
        return MethodResultMessage.class;
    }
}
