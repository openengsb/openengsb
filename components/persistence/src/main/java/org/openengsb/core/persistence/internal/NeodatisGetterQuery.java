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

package org.openengsb.core.persistence.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.neodatis.odb.core.query.nq.NativeQuery;

@SuppressWarnings("serial")
public class NeodatisGetterQuery<TYPE> extends NativeQuery {
    private List<Method> getters;
    private TYPE example;

    public NeodatisGetterQuery(List<Method> getters, TYPE example) {
        this.getters = getters;
        this.example = example;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean match(Object object) {
        TYPE compare = (TYPE) object;
        for (Method method : getters) {
            try {
                Object exampleValue = method.invoke(example);
                Object compareValue = method.invoke(compare);
                if (exampleValue != null && !exampleValue.equals(compareValue)) {
                    return false;
                }
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public Class<?> getObjectType() {
        return example.getClass();
    }
}
