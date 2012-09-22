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

package org.openengsb.core.ekb.persistence.persist.edb.models;

import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBForeignKey;

import com.google.common.base.Objects;

@Model
public class EngineeringObjectModel {

    @OpenEngSBForeignKey(modelType = "org.openengsb.core.ekb.persistence.persist.edb.models.SourceModelA",
        modelVersion = "1.0.0")
    private String modelAId;

    @OpenEngSBForeignKey(modelType = "org.openengsb.core.ekb.persistence.persist.edb.models.SourceModelB",
        modelVersion = "1.0.0")
    private String modelBId;

    private String test;
    private String nameA;
    private String nameB;

    public String getNameA() {
        return this.nameA;
    }

    public void setNameA(String nameA) {
        this.nameA = nameA;
    }

    public String getNameB() {
        return this.nameB;
    }

    public void setNameB(String nameB) {
        this.nameB = nameB;
    }

    public String getModelAId() {
        return modelAId;
    }

    public void setModelAId(String modelAId) {
        this.modelAId = modelAId;
    }

    public String getModelBId() {
        return modelBId;
    }

    public void setModelBId(String modelBId) {
        this.modelBId = modelBId;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
    
    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass()).add("nameA", nameA).add("nameB", nameB)
                .add("modelAId", modelAId).add("modelBId", modelBId).toString();
    }
}
