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
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.common.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter takes a {@link MethodCallMessage} and checks if any parameter is type of OpenEngSBModel. If so, it
 * converts it to the corresponding OpenEngSBModelWrapper. The new object is then passed on to the next filter. The
 * returned {@link MethodResultMessage} is checked for OpenEngSBModelWrapper. If this is the case, it is converted to a
 * OpenEngSBModelObject again.
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
public class EKBProxyOutgoingFilter extends
        AbstractFilterChainElement<MethodCallMessage, MethodResultMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EKBProxyOutgoingFilter.class);

    private FilterAction next;

    @Override
    public MethodResultMessage doFilter(MethodCallMessage input, Map<String, Object> metadata) throws FilterException {
        LOGGER.debug("entered EKBProxyOutgoingFilter");
        Object[] parameters = input.getMethodCall().getArgs();
        if (parameters != null) {
            List<String> classes = input.getMethodCall().getClasses();
            List<String> newClasses = new ArrayList<String>();

            for (int i = 0; i < parameters.length; i++) {
                if (List.class.isAssignableFrom(parameters[i].getClass())) {
                    List<?> list = (List<?>) parameters[i];
                    List<Object> wrappers = new ArrayList<Object>();
                    if (list.size() != 0 && OpenEngSBModel.class.isAssignableFrom(list.get(0).getClass())) {
                        for (int j = 0; j < list.size(); j++) {
                            OpenEngSBModel model = (OpenEngSBModel) list.get(j);
                            OpenEngSBModelWrapper wrapper = ModelUtils.generateWrapperOutOfModel(model);
                            wrappers.add(wrapper);
                        }
                    }
                    parameters[i] = wrappers;
                } else if (OpenEngSBModel.class.isAssignableFrom(parameters[i].getClass())) {
                    LOGGER.debug("try to generate wrapper from model");
                    OpenEngSBModel model = (OpenEngSBModel) parameters[i];
                    OpenEngSBModelWrapper wrapper = ModelUtils.generateWrapperOutOfModel(model);
                    LOGGER.debug("successfully generated wrapper");
                    parameters[i] = wrapper;
                    newClasses.add(OpenEngSBModelWrapper.class.getName());
                } else {
                    if (classes != null && classes.size() >= (i + 1)) {
                        newClasses.add(classes.get(i));
                    } else {
                        newClasses.add(parameters[i].getClass().getName());
                    }
                }
            }
            input.getMethodCall().setArgs(parameters);
            input.getMethodCall().setClasses(newClasses);
        }

        LOGGER.debug("forward to next filter");
        MethodResultMessage message = (MethodResultMessage) next.filter(input, metadata);

        LOGGER.debug("receiving answer from next filter");
        if (message.getResult().getArg() != null
                && message.getResult().getArg().getClass().equals(OpenEngSBModelWrapper.class)) {
            LOGGER.debug("try to generate model out of wrapper");
            OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) message.getResult().getArg();
            Object modelObject = ModelUtils.generateModelOutOfWrapper(wrapper);
            LOGGER.debug("successfully generated model");
            message.getResult().setArg(modelObject);
            message.getResult().setClassName(wrapper.getModelClass());
        } else if (message.getResult().getArg() != null
                && List.class.isAssignableFrom(message.getResult().getArg().getClass())) {
            List<?> list = (List<?>) message.getResult().getArg();
            List<Object> arg = new ArrayList<Object>();
            if (list.size() > 0 && OpenEngSBModel.class.isAssignableFrom(list.get(0).getClass())) {
                for (int j = 0; j < list.size(); j++) {
                    OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) list.get(j);
                    Object model = ModelUtils.generateModelOutOfWrapper(wrapper);
                    arg.add(model);
                }
                message.getResult().setArg(arg);
            }
        }
        LOGGER.debug("leaving EKBProxyIncomingFilter");
        return message;
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, MethodCallMessage.class, MethodResultMessage.class);
        this.next = next;
    }
}
