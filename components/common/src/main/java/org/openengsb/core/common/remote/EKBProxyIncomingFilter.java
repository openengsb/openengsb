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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelWrapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.common.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter takes a {@link MethodCallRequest} and checks if any parameter is type of OpenEngSBModelWrapper. If so, it
 * converts it to the corresponding OpenEngSBModelObject. The new object is then passed on to the next filter. The
 * returned {@link MethodResultMessage} is checked for OpenEngSBModelObject. If this is the case, it is converted to a
 * OpenEngSBModelWrapper again.
 * 
 * <code>
 * <pre>
 *      [MethodCallRequest]   > Filter > [MethodCallRequest]     > ...
 *                                                                  |
 *                                                                  v
 *      [MethodResultMessage] < Filter < [MethodResultMessage]   < ...
 * </pre>
 * </code>
 */
public class EKBProxyIncomingFilter extends
        AbstractFilterChainElement<MethodCallRequest, MethodResultMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EKBProxyIncomingFilter.class);

    private FilterAction next;

    public EKBProxyIncomingFilter() {
        super(MethodCallRequest.class, MethodResultMessage.class);
    }

    @Override
    public MethodResultMessage doFilter(MethodCallRequest input, Map<String, Object> metadata) throws FilterException {
        Object[] parameters = input.getMethodCall().getArgs();
        LOGGER.debug("entered EKBProxyIncomingFilter");
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (List.class.isAssignableFrom(parameters[i].getClass())) {
                    List<?> list = (List<?>) parameters[i];
                    List<Object> models = new ArrayList<Object>();
                    if (list.size() != 0 && list.get(0).getClass().equals(OpenEngSBModelWrapper.class)) {

                        for (int j = 0; j < list.size(); j++) {
                            OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) parameters[i];
                            Object model = ModelUtils.generateModelOutOfWrapper(wrapper);
                            models.add(model);
                        }
                    }
                    parameters[i] = models;
                } else if (parameters[i].getClass().equals(OpenEngSBModelWrapper.class)) {
                    LOGGER.debug("try to generate model out of wrapper");
                    OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) parameters[i];
                    Object model = ModelUtils.generateModelOutOfWrapper(wrapper);
                    LOGGER.debug("successfully generated model");
                    parameters[i] = model;
                }
            }
            input.getMethodCall().setArgs(parameters);
        }

        LOGGER.debug("forward to next filter");
        MethodResultMessage message = (MethodResultMessage) next.filter(input, metadata);

        LOGGER.debug("receiving answer from next filter");
        if (message.getResult().getArg() != null
                && OpenEngSBModel.class.isAssignableFrom(message.getResult().getArg().getClass())) {
            LOGGER.debug("try to generate wrapper from model");
            OpenEngSBModel model = (OpenEngSBModel) message.getResult().getArg();
            OpenEngSBModelWrapper wrapper = ModelUtils.generateWrapperOutOfModel(model);

            message.getResult().setArg(wrapper);
            LOGGER.debug("successfully generated wrapper");
        } else if (message.getResult().getArg() != null
                && List.class.isAssignableFrom(message.getResult().getArg().getClass())) {
            List<?> list = (List<?>) message.getResult().getArg();
            List<Object> arg = new ArrayList<Object>();
            if (list.size() > 0 && OpenEngSBModel.class.isAssignableFrom(list.get(0).getClass())) {
                for (int j = 0; j < list.size(); j++) {
                    OpenEngSBModel model = (OpenEngSBModel) list.get(j);
                    OpenEngSBModelWrapper wrapper = ModelUtils.generateWrapperOutOfModel(model);
                    arg.add(wrapper);
                }
                message.getResult().setArg(arg);
            }
        }
        LOGGER.debug("leaving EKBProxyIncomingFilter");
        return message;
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, MethodCallRequest.class, MethodResultMessage.class);
        this.next = next;
    }
}
