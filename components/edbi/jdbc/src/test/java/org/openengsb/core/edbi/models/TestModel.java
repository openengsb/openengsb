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
package org.openengsb.core.edbi.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;

public class TestModel implements OpenEngSBModel {

    @OpenEngSBModelId
    private String testId;
    private Integer testInteger;

    private CompositeTestModel compositeModel;

    public TestModel() {

    }

    public TestModel(String id) {
        setTestId(id);
    }

    public TestModel(String id, Integer integer) {
        setTestId(id);
        setTestInteger(integer);
    }

    public CompositeTestModel getCompositeModel() {
        return compositeModel;
    }

    public void setCompositeModel(CompositeTestModel compositeModel) {
        this.compositeModel = compositeModel;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public Integer getTestInteger() {
        return testInteger;
    }

    public void setTestInteger(Integer testInteger) {
        this.testInteger = testInteger;
    }

    @Override
    public List<OpenEngSBModelEntry> toOpenEngSBModelValues() {
        return Arrays.asList(
            new OpenEngSBModelEntry("testId", getTestId(), String.class),
            new OpenEngSBModelEntry("testInteger", getTestInteger(), Integer.class),
            new OpenEngSBModelEntry("compositeModel", getCompositeModel(), CompositeTestModel.class));
    }

    @Override
    public List<OpenEngSBModelEntry> toOpenEngSBModelEntries() {
        return toOpenEngSBModelValues();
    }

    @Override
    public Object retrieveInternalModelId() {
        return getTestId();
    }

    @Override
    public Long retrieveInternalModelTimestamp() {
        return 0L;
    }

    @Override
    public Integer retrieveInternalModelVersion() {
        return 1;
    }

    @Override
    public void addOpenEngSBModelEntry(OpenEngSBModelEntry entry) {
    }

    @Override
    public void removeOpenEngSBModelEntry(String key) {
    }

    @Override
    public List<OpenEngSBModelEntry> getOpenEngSBModelTail() {
        return new ArrayList<>();
    }

    @Override
    public void setOpenEngSBModelTail(List<OpenEngSBModelEntry> entries) {
    }

    @Override
    public String retrieveModelName() {
        return getClass().getName();
    }

    @Override
    public String retrieveModelVersion() {
        return "1.0.0";
    }
}
