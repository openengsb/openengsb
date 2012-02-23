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
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.openengsb.core.common.util.ModelUtils;

public class XLinkServerSideTest {
    

    @Test
    public void testXLinkURLAsTemplateContainsPlaceHolders_withOOIdentfier(){
        XLinkUrl testUrl = ModelUtils.createEmptyModelObject(XLinkUrl.class);
        
        testUrl.setUrl("http://openengsb.org/registryServlet.html");
        
        ExampleObjectOrientedIdentifier ooIdentifier = ModelUtils.createEmptyModelObject(ExampleObjectOrientedIdentifier.class);
        ooIdentifier.setValidTill(XLinkUtils.returnValidTillTimeStamp());
        ooIdentifier.setIdentifierId("ExampleObjectOrientedIdentifier");
        testUrl.setIdentifier(ooIdentifier);
        
        String urlTemplate = XLinkUtils.returnXLinkUrl(testUrl);
        
        //urlWithParams is http://openengsb.org/registryServlet.html?OOMethodName=$$OOMethodName$$&OOClassName=$$OOClassName$$&OOPackageName=$$OOPackageName$$&identifierId=ExampleObjectOrientedIdentifier&validTill=20120226132956

        
        assertTrue(urlTemplate.contains("OOMethodName=$$OOMethodName$$"));
        assertTrue(urlTemplate.contains("OOClassName=$$OOClassName$$"));
        assertTrue(urlTemplate.contains("OOPackageName=$$OOPackageName$$"));
    }
    
    @Test
    public void testPreparedXLinkUrlIsNOtDulyCompleted_withOOIdentfier(){
        XLinkUrl testUrl = ModelUtils.createEmptyModelObject(XLinkUrl.class);
        
        testUrl.setUrl("http://openengsb.org/registryServlet.html");
        
        ExampleObjectOrientedIdentifier ooIdentifier = ModelUtils.createEmptyModelObject(ExampleObjectOrientedIdentifier.class);
        ooIdentifier.setValidTill(XLinkUtils.returnValidTillTimeStamp());
        ooIdentifier.setIdentifierId("ExampleObjectOrientedIdentifier");
        testUrl.setIdentifier(ooIdentifier);
        
        assertFalse(XLinkUtils.isUrlDulyCompleted(testUrl));
    }

}
