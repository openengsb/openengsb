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

package org.openengsb.core.ekb.common.models;

import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;

@Model
public class WrappedPropertiesModel {

    @OpenEngSBModelId
    private String id;

    private Boolean booleanByGet;
    private Boolean booleanByIs;
    private Short wrappedShort;
    private Integer wrappedInt;
    private Long wrappedLong;
    private Double wrappedDouble;
    private Float wrappedFloat;
    private Character wrappedChar;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getBooleanByGet() {
        return booleanByGet;
    }

    public void setBooleanByGet(Boolean booleanByGet) {
        this.booleanByGet = booleanByGet;
    }

    public Boolean isBooleanByIs() {
        return booleanByIs;
    }

    public void setBooleanByIs(Boolean booleanByIs) {
        this.booleanByIs = booleanByIs;
    }

    public Short getWrappedShort() {
        return wrappedShort;
    }

    public void setWrappedShort(Short wrappedShort) {
        this.wrappedShort = wrappedShort;
    }

    public Integer getWrappedInt() {
        return wrappedInt;
    }

    public void setWrappedInt(Integer wrappedInt) {
        this.wrappedInt = wrappedInt;
    }

    public Long getWrappedLong() {
        return wrappedLong;
    }

    public void setWrappedLong(Long wrappedLong) {
        this.wrappedLong = wrappedLong;
    }

    public Double getWrappedDouble() {
        return wrappedDouble;
    }

    public void setWrappedDouble(Double wrappedDouble) {
        this.wrappedDouble = wrappedDouble;
    }

    public Float getWrappedFloat() {
        return wrappedFloat;
    }

    public void setWrappedFloat(Float wrappedFloat) {
        this.wrappedFloat = wrappedFloat;
    }

    public Character getWrappedChar() {
        return wrappedChar;
    }

    public void setWrappedChar(Character wrappedChar) {
        this.wrappedChar = wrappedChar;
    }

}
