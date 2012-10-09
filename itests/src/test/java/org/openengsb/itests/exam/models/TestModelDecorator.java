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

package org.openengsb.itests.exam.models;

import java.util.List;

import org.apache.commons.beanutils.MethodUtils;

/**
 * The TestModelDecorator is a helper class for easier working with the TestModel class since it does the java
 * reflection calls for you on the TestModel object class instance
 */
public class TestModelDecorator {
    private Object model;

    public TestModelDecorator(Object model) {
        this.model = model;
    }

    public Object getModel() {
        return model;
    }

    public void setName(String name) throws Exception {
        MethodUtils.invokeMethod(model, "setName", name);
    }

    public String getName() throws Exception {
        return (String) MethodUtils.invokeMethod(model, "getName", null);
    }

    public void setEdbId(String edbId) throws Exception {
        MethodUtils.invokeMethod(model, "setEdbId", edbId);
    }

    public String getEdbId() throws Exception {
        return (String) MethodUtils.invokeMethod(model, "getEdbId", null);
    }

    public void setSubModel(Object subModel) throws Exception {
        MethodUtils.invokeMethod(model, "setSubModel", subModel);
    }

    public Object getSubModel() throws Exception {
        return MethodUtils.invokeMethod(model, "getSubModel", null);
    }

    public void setSubs(List<Object> subs) throws Exception {
        MethodUtils.invokeMethod(model, "setSubs", subs);
    }

    @SuppressWarnings("unchecked")
    public List<Object> getSubs() throws Exception {
        return (List<Object>) MethodUtils.invokeMethod(model, "getSubs", null);
    }

    public void setIds(List<Integer> ids) throws Exception {
        MethodUtils.invokeMethod(model, "setIds", ids);
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getIds() throws Exception {
        return (List<Integer>) MethodUtils.invokeMethod(model, "getIds", null);
    }
}
