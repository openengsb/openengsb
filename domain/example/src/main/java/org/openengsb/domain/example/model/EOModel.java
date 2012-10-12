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

package org.openengsb.domain.example.model;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBForeignKey;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.openengsb.labs.delegation.service.Provide;

@Provide(context = { Constants.DELEGATION_CONTEXT_MODELS })
@Model
public class EOModel {
    @OpenEngSBModelId
    private String edbId;
    @OpenEngSBForeignKey(modelType = SourceModelA.class, modelVersion = "3.0.0.SNAPSHOT")
    private String refModelA;
    @OpenEngSBForeignKey(modelType = SourceModelB.class, modelVersion = "3.0.0.SNAPSHOT")
    private String refModelB;
    private String nameA;
    private String nameB;

    public String getEdbId() {
        return edbId;
    }

    public void setEdbId(String edbId) {
        this.edbId = edbId;
    }

    public String getRefModelA() {
        return refModelA;
    }

    public void setRefModelA(String refModelA) {
        this.refModelA = refModelA;
    }

    public String getRefModelB() {
        return refModelB;
    }

    public void setRefModelB(String refModelB) {
        this.refModelB = refModelB;
    }

    public String getNameA() {
        return nameA;
    }

    public void setNameA(String nameA) {
        this.nameA = nameA;
    }

    public String getNameB() {
        return nameB;
    }

    public void setNameB(String nameB) {
        this.nameB = nameB;
    }
}
