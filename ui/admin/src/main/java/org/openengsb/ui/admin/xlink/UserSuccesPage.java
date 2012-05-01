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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openengsb.ui.admin.xlink.mocking.XLinkMock;
import org.openengsb.ui.admin.xlink.exceptions.OpenXLinkException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.xlink.XLinkModelInformation;
import org.openengsb.core.api.xlink.XLinkRegisteredTool;
import org.openengsb.core.api.xlink.XLinkToolView;
import org.openengsb.core.common.xlink.XLinkUtils;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

/**
 */
@PaxWicketMountPoint(mountPoint = "openXLink")
public class UserSuccesPage extends WebPage{
    
    private String contextId;
    private String modelId;
    private String versionId;
    private Calendar expirationDate;
    private String hostId;
    
    private String connectorId;
    private String viewId;

    private Map<String,String> identifierValues;
    
    private Map<String,String[]> requestParameters;
    
    public UserSuccesPage(){
        preProcessingPage();
    }
    
    
    public UserSuccesPage(PageParameters parameters) {
        preProcessingPage();
    }
    
    public void preProcessingPage(){
        
        requestParameters = getRequest().getParameterMap();
        setLocale(getRequest().getLocale());
        
        HttpServletRequest req = ((WebRequest)getRequest()).getHttpServletRequest();
        HttpServletResponse resp = ((WebResponse)getResponse()).getHttpServletResponse();
        
        try {
            checkIfXLinkIsValid(req);
        } catch (OpenXLinkException ex) {
            handleErrorResponse(ex.getMessage());
            return;
        }
        setContextFromId();
        if(checkForLocalSwitchingParameters()){
            String sourceModelClass = modelId;           
            XLinkModelInformation destinationModelClass = XLinkMock.getDestinationModelClass(connectorId,viewId);
            XLinkMock.transformAndOpenMatch(sourceModelClass, versionId, identifierValues, destinationModelClass.getClassName(), connectorId, viewId);
            handleSuccessResponse(resp);
            return;
        }
        handleSuccessResponse(resp);      
    }
    
    private String getParameterFromMap(String key){
        return (requestParameters.get(key) == null ? null : requestParameters.get(key)[0]);
    }
    
    private void checkIfXLinkIsValid(HttpServletRequest req) throws OpenXLinkException{
        fetchXLinkParameters(req);
        checkMandatoryXLinkParameters();
        checkXLinkIsExpired();
        try {
            fetchAndCheckIdentifier(XLinkMock.getModelIdentifierToModelId(modelId,versionId));
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UserSuccesPage.class.getName()).log(Level.SEVERE, null, ex);
            throw new OpenXLinkException(ex.getMessage());
        }
    }
    
    private void fetchXLinkParameters(HttpServletRequest req){
        contextId = getParameterFromMap(XLinkUtils.XLINK_CONTEXTID_KEY);
        modelId = getParameterFromMap(XLinkUtils.XLINK_MODELCLASS_KEY);
        versionId = getParameterFromMap(XLinkUtils.XLINK_VERSION_KEY);
        expirationDate = XLinkUtils.dateStringToCalendar(getParameterFromMap(XLinkUtils.XLINK_EXPIRATIONDATE_KEY));
        hostId = req.getRemoteHost();//.getHeader(XLinkUtils.XLINK_HOST_HEADERNAME);
        connectorId = getParameterFromMap(XLinkUtils.XLINK_CONNECTORID_KEY);
        viewId = getParameterFromMap(XLinkUtils.XLINK_VIEW_KEY);
    }    

    private void checkMandatoryXLinkParameters() throws OpenXLinkException{
        String errorMsg = new StringResourceModel("error.missingMandatoryGetParam", this, null).getString();
        if(contextId == null)errorMsg+=" "+XLinkUtils.XLINK_CONTEXTID_KEY;
        if(modelId == null)errorMsg+=", "+XLinkUtils.XLINK_MODELCLASS_KEY;
        if(versionId == null)errorMsg+=", "+XLinkUtils.XLINK_VERSION_KEY;
        if(expirationDate == null)errorMsg+=", "+XLinkUtils.XLINK_EXPIRATIONDATE_KEY;
        if(hostId == null)errorMsg+=", "+XLinkUtils.XLINK_HOST_HEADERNAME;
        if((contextId == null)||(modelId == null)||(versionId == null)||(expirationDate == null)||(hostId == null)){
            throw new OpenXLinkException(errorMsg);
        }
    }    
    
    private void checkXLinkIsExpired() throws OpenXLinkException{
        if((expirationDate != null ? Calendar.getInstance().after(expirationDate) : false)){
            String expiredMsg = new StringResourceModel("error.xlinkHasExpired", this, null).getString();
            throw new OpenXLinkException(expiredMsg);
        }        
    }    
    
    private void fetchAndCheckIdentifier(List<String> identifierKeyNames) throws OpenXLinkException{
        String errorMsgFormat = new StringResourceModel("error.missingIdentifier", this, null).getString();
        String errorMsg = "";
        identifierValues = new HashMap();
        for(String key : identifierKeyNames){
            String currentValue = getParameterFromMap(key);
            if(currentValue == null){
                errorMsg += String.format(errorMsgFormat,key);
            }
            identifierValues.put(key, currentValue);
        }
        if(identifierValues.containsValue(null))throw new OpenXLinkException(errorMsg);
    }
    
    private boolean checkForLocalSwitchingParameters(){
        return (connectorId != null)&&(viewId != null);
    }
    
    private void handleErrorResponse(String error){
        if(checkForLocalSwitchingParameters()){
            setResponsePage(new MachineResponsePage(error,false));
        }else{   
            setResponsePage(new UserErrorPage(error));        
        }
    }
    
    private void handleSuccessResponse(HttpServletResponse resp){
        if(checkForLocalSwitchingParameters()){
            String successMsg = new StringResourceModel("success.localSwitch", this, null).getString();
            setResponsePage(new MachineResponsePage(successMsg,true));
        }else{         
            List<XLinkRegisteredTool> tools = XLinkMock.getRegisteredToolsFromUser(hostId);
            ListView toolList = new ListView("toolList", tools) {
                protected void populateItem(ListItem item) {
                    XLinkRegisteredTool tool = (XLinkRegisteredTool) item.getModelObject();
                    item.add(new Label("toolName", tool.getToolName()));
                    ListView viewList = new ListView("viewList", tool.getAvailableViews()) {
                        @Override
                        protected void populateItem(ListItem li) {
                            XLinkToolView view = (XLinkToolView) li.getModelObject();
                            li.add(new Label("viewName", view.getName()));
                            li.add(new Label("viewDescription", view.getDescriptions().get("en")));
                        }
                    };
                    item.add(viewList);
                }
            };
            add(toolList);            
                /*
                 * 5) fetch input of select page
                 * 6) call parser and return thank you page
                 */
            //weiterleiten der registrierten tools, der modelId, der version, der contextId            
        }        

     
    }
    
    public void setContextFromId(){
        ContextHolder.get().setCurrentContextId(contextId);
    }
    
    public void setLocale(Locale locale){
        if (locale != null){
            getSession().setLocale(locale);
        }
    }    
    
    public String getLocaleKey(){
        return getSession().getLocale().getLanguage();
    }
    
}
