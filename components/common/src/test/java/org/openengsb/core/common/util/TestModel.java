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

package org.openengsb.core.common.util;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * little interface for testing the proxy functionality of the EKBServiceTest
 */
interface TestModel extends OpenEngSBModel {
        
    void setId(String id);
    
    String getId();
    
    void setDate(Date date);
    
    Date getDate();
    
    String getName();
    
    void setName(String name);
    
    void testMethod();
    
    void setTest(String test);
    
    String getTest();
    
    void setEnumeration(ENUM enumeration);
    
    ENUM getEnumeration();
    
    void setList(List<String> list);
    
    List<String> getList();

    void setSub(SubModel sub);
    
    SubModel getSub();
    
    void setSubs(List<SubModel> subs);
    
    List<SubModel> getSubs();
    
    void setFile(File file);
    
    File getFile();
    
    Map<String, String> getMap();
    
    void setMap(Map<String, String> map);
}

enum ENUM {
    A,
    B,
    C
}
