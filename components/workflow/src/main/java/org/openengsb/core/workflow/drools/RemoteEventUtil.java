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

package org.openengsb.core.workflow.drools;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.openengsb.core.api.Event;
import org.openengsb.core.workflow.api.model.RemoteEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public final class RemoteEventUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteEventUtil.class);

    private RemoteEventUtil() {
    }

    public static RemoteEvent wrapEvent(Event event) {
        RemoteEvent result = new RemoteEvent(event.getClass().getName());
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(event.getClass());
        Map<String, String> nestedEventProperties = result.getNestedEventProperties();
        for (PropertyDescriptor pd : propertyDescriptors) {
            Method readMethod = pd.getReadMethod();
            LOGGER.debug("writing property {} to event", pd.getName());
            try {
                Object value = readMethod.invoke(event);
                if (value != null) {
                    nestedEventProperties.put(pd.getName(), value.toString());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

}
