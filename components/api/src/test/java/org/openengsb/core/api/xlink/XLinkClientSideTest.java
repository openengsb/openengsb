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

package org.openengsb.core.api.xlink;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.util.ModelUtils;

public class XLinkClientSideTest {
    
    
    XLinkUrl testUrl;
    
    @Before
    public void setUp() {
        testUrl = ModelUtils.createEmptyModelObject(XLinkUrl.class);
        testUrl.setUrl("http://openengsb.org/registryServlet.html");
        ExampleObjectOrientedIdentifier ooIdentifier = ModelUtils.createEmptyModelObject(ExampleObjectOrientedIdentifier.class);
        ooIdentifier.setValidTill(XLinkUtils.returnValidTillTimeStamp());
        ooIdentifier.setIdentifierId("ExampleObjectOrientedIdentifier");
        testUrl.setIdentifier(ooIdentifier);
    }   
    
    @Test
    public void testPreparedXLinkUrlContainsCorrectIdentifier(){
        assertTrue(testUrl.getIdentifier() instanceof ExampleObjectOrientedIdentifier);
    }
    
    @Test
    public void testFillXLinkUrlWithParams_withOOIdentfier(){
        ((ExampleObjectOrientedIdentifier)testUrl.getIdentifier()).setOOClassName("hugoClass");
        ((ExampleObjectOrientedIdentifier)testUrl.getIdentifier()).setOOMethodName("hugoMethod");
        ((ExampleObjectOrientedIdentifier)testUrl.getIdentifier()).setOOPackageName("hugoPackage");
        String urlWithParams= XLinkUtils.returnXLinkUrl(testUrl);
       
        //urlWithParams is http://openengsb.org/registryServlet.html?oOClassName=hugoClass&oOMethodName=hugoMethod&identifierId=ExampleObjectOrientedIdentifier&validTill=20120226132956&oOPackageName=hugoPackage
        
        assertTrue(urlWithParams.contains("oOMethodName=hugoMethod"));
        assertTrue(urlWithParams.contains("oOClassName=hugoClass"));
        assertTrue(urlWithParams.contains("oOPackageName=hugoPackage"));
    }
    
    @Test
    public void testFilledXLinkUrlIsDulyCompleted_withOOIdentfier(){
        ((ExampleObjectOrientedIdentifier)testUrl.getIdentifier()).setOOClassName("hugoClass");
        ((ExampleObjectOrientedIdentifier)testUrl.getIdentifier()).setOOMethodName("hugoMethod");
        ((ExampleObjectOrientedIdentifier)testUrl.getIdentifier()).setOOPackageName("hugoPackage");
        
        assertTrue(XLinkUtils.isUrlDulyCompleted(testUrl));
    }

}
