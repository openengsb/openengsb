/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.workflow.model.RemoteEvent;
import org.springframework.beans.BeanUtils;

public class RemoteEventUtil {

    private static Log log = LogFactory.getLog(RemoteEventUtil.class);

    public static RemoteEvent wrapEvent(Event event) {
        RemoteEvent result = new RemoteEvent(event.getClass().getName());
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(event.getClass());
        Map<String, String> nestedEventProperties = result.getNestedEventProperties();
        for (PropertyDescriptor pd : propertyDescriptors) {
            Method readMethod = pd.getReadMethod();
            log.debug(String.format("writing property %s to event", pd.getName()));
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
