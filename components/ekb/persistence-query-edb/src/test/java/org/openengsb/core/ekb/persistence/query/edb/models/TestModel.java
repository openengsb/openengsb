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

package org.openengsb.core.ekb.persistence.query.edb.models;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.openengsb.core.ekb.persistence.query.edb.models.TestModel2.ENUM;

/**
 * Model for testing the proxy functionality of the EKBServiceTest
 */
@Model
public class TestModel {
    @OpenEngSBModelId
    private String id;
    private Date date;
    private String name;
    private String test;
    private ENUM enumeration;
    private List<String> list;
    private SubModel sub;
    private List<SubModel> subs;
    private Map<String, String> map;
    private int number;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void testMethod() {

    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public ENUM getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(ENUM enumeration) {
        this.enumeration = enumeration;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public SubModel getSub() {
        return sub;
    }

    public void setSub(SubModel sub) {
        this.sub = sub;
    }

    public List<SubModel> getSubs() {
        return subs;
    }

    public void setSubs(List<SubModel> subs) {
        this.subs = subs;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
    
    public int getNumber() {
        return number;
    }
    
    public void setNumber(int number) {
        this.number = number;
    }
}
