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

package org.openengsb.ui.admin.xlink;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.xlink.ExampleObjectOrientedModel;
import org.openengsb.core.common.xlink.XLinkUtils;
import org.openengsb.ui.admin.AbstractUITest;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class ToolChooserTest extends AbstractUITest {
    
    private final String dateFormat = "yyyyMMddkkmmss";
    
    @Before
    public void setup() throws Exception {
        setupTesterWithSpringMockContext();
    }

    private void setupCommonXLinkParams(PageParameters params) {
        params.add(XLinkUtils.XLINK_EXPIRATIONDATE_KEY, getExpirationDate(3));
        params.add(XLinkUtils.XLINK_MODELCLASS_KEY, ExampleObjectOrientedModel.class.getName());
        params.add(XLinkUtils.XLINK_VERSION_KEY, "1.0");
        params.add(XLinkUtils.XLINK_CONTEXTID_KEY, "ExampleContext");         
    }
    
    private void setupLocalSwitchXLinkParams(PageParameters params) {
        params.add(XLinkUtils.XLINK_CONNECTORID_KEY, "test2+test2+test2");
        params.add(XLinkUtils.XLINK_VIEW_KEY, "exampleViewId_1");        
    }    
    
    private void setupIdentfierParamsForExampleOOModel(PageParameters params) {
        params.add("OOMethodName", "testMethod");
        params.add("OOClassName", "testClass");
        params.add("OOPackageName", "testPackage");          
    }    
    
    private void setupNessecaryHeader() {
        tester.addRequestHeader(XLinkUtils.XLINK_HOST_HEADERNAME, "localhost:8090");
    }
    
    private void setupTesterWithSpringMockContext() {
        tester.getApplication().getComponentInstantiationListeners()
            .add(new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
    }    
    
    private String getExpirationDate(int futureDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, futureDays);
        Format formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(calendar.getTime());
    }    
    
    @Test
    public void openToolChooserPage_isRenderedWithSuccessfullLink() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(ToolChooserPage.class);
        tester.assertContains("Tool B");
        tester.assertContains("View 2");
    }
    
    @Test
    public void openToolChooserPage_missingGetParam_Version() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_VERSION_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkUtils.XLINK_VERSION_KEY);
    }
  
    @Test
    public void openToolChooserPage_missingGetParam_Date() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_EXPIRATIONDATE_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkUtils.XLINK_EXPIRATIONDATE_KEY);
    }
    
    @Test
    public void openToolChooserPage_missingGetParam_ModelClass() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_MODELCLASS_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkUtils.XLINK_MODELCLASS_KEY);
    }
    
    @Test
    public void openToolChooserPage_missingGetParam_Context() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_CONTEXTID_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkUtils.XLINK_CONTEXTID_KEY);
    }      
    
    @Test
    public void openToolChooserPage_missingIdentifier_ExampleModel_Package() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove("OOPackageName");
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains("OOPackageName");
        tester.assertContains("Identifier");
    }   
    
    @Test
    public void openToolChooserPage_missingIdentifier_ExampleModel_Method() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove("OOMethodName");
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains("OOMethodName");
        tester.assertContains("Identifier");
    }  
    
    @Test
    public void openToolChooserPage_missingIdentifier_ExampleModel_Class() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove("OOClassName");
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains("OOClassName");
        tester.assertContains("Identifier");
    }      
    
    @Test
    public void openLocalSwitchPage_isRenderedWithSuccessfullLink() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupLocalSwitchXLinkParams(params);
        setupNessecaryHeader();
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(MachineResponsePage.class);
    }    
    
}
