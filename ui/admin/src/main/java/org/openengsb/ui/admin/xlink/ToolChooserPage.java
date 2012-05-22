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



import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.xlink.model.XLinkLocalTool;
import org.openengsb.core.api.xlink.model.XLinkModelInformation;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.openengsb.core.common.xlink.XLinkUtils;
import org.openengsb.ui.admin.xlink.exceptions.OpenXLinkException;
import org.openengsb.ui.admin.xlink.mocking.XLinkMock;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

/**
 */
@PaxWicketMountPoint(mountPoint = "openXLink")
public class ToolChooserPage extends WebPage {
    
    @PaxWicketBean(name = "serviceManager")
    private ConnectorManager serviceManager;
    private ToolChooserLogic chooserLogic;
    
    private String contextId;
    private String modelId;
    private String versionId;
    private Calendar expirationDate;
    private String hostId;
    
    private String connectorId;
    private String viewId;

    private Map<String, String> identifierValues;
    
    private Map<String, String[]> requestParameters;
    
    public ToolChooserPage() {
        processPage();
    }
    
    
    public ToolChooserPage(PageParameters parameters) {
        processPage();
    }
    
    private void processPage() {
        XLinkMock.dummyRegistrationOfTools(serviceManager);
        chooserLogic = new ToolChooserLogic(serviceManager);
        requestParameters = getRequestParametersAsAMap();
        HttpServletRequest req = (HttpServletRequest) getRequest().getContainerRequest();
        HttpServletResponse resp = (HttpServletResponse) getResponse().getContainerResponse();
        try {
            checkIfXLinkIsValid(req);
        } catch (OpenXLinkException ex) {
            handleErrorResponse(ex.getMessage());
            return;
        }
        //setContextFromId();
        if (checkForLocalSwitchingParameters()) {
            String sourceModelClass = modelId;           
            XLinkModelInformation destinationModelClass = chooserLogic.getModelClassOfView(hostId, connectorId, viewId);
            XLinkMock.transformAndOpenMatch(sourceModelClass, 
                    versionId, identifierValues, destinationModelClass.getClassName(), 
                    destinationModelClass.getVersion(), connectorId, viewId);
            handleSuccessResponse(resp);
            return;
        }
        buildToolChooserPage(resp);      
    }
    
    private Map<String, String[]> getRequestParametersAsAMap() {
        Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        IRequestParameters parameters = getRequest().getQueryParameters();
        for (String key : parameters.getParameterNames()) {
            List<StringValue> values = parameters.getParameterValues(key);
            List<String> valuesAsString = new ArrayList<String>();
            for (StringValue stringvalue : values) {
                valuesAsString.add(stringvalue.toString());
            }
            parameterMap.put(key, (String[]) valuesAsString.toArray(new String[0]));
        }
        return parameterMap;
    }
    
    private String getParameterFromMap(String key) {
        return requestParameters.get(key) == null ? null : requestParameters.get(key)[0];
    }
    
    private void checkIfXLinkIsValid(HttpServletRequest req) throws OpenXLinkException {
        fetchXLinkParameters(req);
        checkMandatoryXLinkParameters();
        /*if (checkForLocalSwitchingParameters()) {
            checkConnectorIdFormat();
        }*/
        checkXLinkIsExpired();
        try {
            fetchAndCheckIdentifier(chooserLogic.getModelIdentifierToModelId(modelId, versionId));
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ToolChooserPage.class.getName()).log(Level.SEVERE, null, ex);
            throw new OpenXLinkException(ex.getMessage());
        }
    }
    
    private void fetchXLinkParameters(HttpServletRequest req) {
        contextId = getParameterFromMap(XLinkUtils.XLINK_CONTEXTID_KEY);
        modelId = getParameterFromMap(XLinkUtils.XLINK_MODELCLASS_KEY);
        versionId = getParameterFromMap(XLinkUtils.XLINK_VERSION_KEY);
        expirationDate = XLinkUtils.dateStringToCalendar(getParameterFromMap(XLinkUtils.XLINK_EXPIRATIONDATE_KEY));
        hostId = req.getHeader(XLinkUtils.XLINK_HOST_HEADERNAME); 
        connectorId = getParameterFromMap(XLinkUtils.XLINK_CONNECTORID_KEY);
        viewId = getParameterFromMap(XLinkUtils.XLINK_VIEW_KEY);
    }    

    private void checkMandatoryXLinkParameters() throws OpenXLinkException {
        String errorMsg = new StringResourceModel("error.missingMandatoryGetParam", this, null).getString();
        if (contextId == null) { errorMsg += " " + XLinkUtils.XLINK_CONTEXTID_KEY; }
        if (modelId == null) { errorMsg += ", " + XLinkUtils.XLINK_MODELCLASS_KEY; }
        if (versionId == null) { errorMsg += ", " + XLinkUtils.XLINK_VERSION_KEY; }
        if (expirationDate == null) { errorMsg += ", " + XLinkUtils.XLINK_EXPIRATIONDATE_KEY; }
        if (hostId == null) { errorMsg += ", " + XLinkUtils.XLINK_HOST_HEADERNAME; }
        if ((contextId == null) 
                || (modelId == null) 
                || (versionId == null) 
                || (expirationDate == null) 
                || (hostId == null)) {
            throw new OpenXLinkException(errorMsg);
        }
    }    
    
    private void checkXLinkIsExpired() throws OpenXLinkException {
        if (expirationDate != null ? Calendar.getInstance().after(expirationDate) : false) {
            String expiredMsg = new StringResourceModel("error.xlinkHasExpired", this, null).getString();
            throw new OpenXLinkException(expiredMsg);
        }        
    }    
    
    /*private void checkConnectorIdFormat() throws OpenXLinkException {
        try {
            ConnectorId.fromFullId(connectorId);
        } catch (Exception e) {
            String wrongFormatMsg = new StringResourceModel("error.connectorId.wrongFormat", this, null).getString();
            throw new OpenXLinkException(wrongFormatMsg);
        }
    }*/
    
    private void fetchAndCheckIdentifier(List<String> identifierKeyNames) throws OpenXLinkException {
        String errorMsgFormat = new StringResourceModel("error.missingIdentifier", this, null).getString();
        String errorMsg = "";
        identifierValues = new HashMap();
        for (String key : identifierKeyNames) {
            String currentValue = getParameterFromMap(key);
            if (currentValue == null) {
                errorMsg += String.format(errorMsgFormat, key);
            }
            identifierValues.put(key, currentValue);
        }
        if (identifierValues.containsValue(null)) {
            throw new OpenXLinkException(errorMsg);
        }
    }
    
    private boolean checkForLocalSwitchingParameters() {
        return (connectorId != null) && (viewId != null);
    }
    
    private void handleErrorResponse(String error) {
        if (checkForLocalSwitchingParameters()) {
            setResponsePage(new MachineResponsePage(error, false));
        } else {   
            String hostIdMsg = new StringResourceModel("hostId.info", this, null).getString();
            hostIdMsg = String.format(hostIdMsg, hostId);              
            setResponsePage(new UserResponsePage(error, hostIdMsg, true));        
        }
    }
    
    private void handleSuccessResponse(HttpServletResponse resp) {
        if (checkForLocalSwitchingParameters()) {
            String successMsg = new StringResourceModel("success.localSwitch", this, null).getString();
            setResponsePage(new MachineResponsePage(successMsg, true));
        } else {         
            String successMsg = new StringResourceModel("success.normalSwitch", this, null).getString();
            String hostIdMsg = new StringResourceModel("hostId.info", this, null).getString();
            hostIdMsg = String.format(hostIdMsg, hostId);            
            setResponsePage(new UserResponsePage(successMsg, hostIdMsg, false));                  
        }        

     
    }
    
    private void buildToolChooserPage(final HttpServletResponse resp) {
        String hostIdMsg = new StringResourceModel("hostId.info", this, null).getString();
        hostIdMsg = String.format(hostIdMsg, hostId);
        add(new Label("hostId", hostIdMsg));
        List<XLinkLocalTool> tools = chooserLogic.getRegisteredToolsFromHost(hostId);    
        ListView toolList = new ListView("toolList", tools) {
            protected void populateItem(ListItem item) {
                final XLinkLocalTool tool = (XLinkLocalTool) item.getModelObject();
                item.add(new Label("toolName", tool.getToolName()));
                ListView viewList = new ListView("viewList", tool.getAvailableViews()) {
                    @Override
                    protected void populateItem(ListItem li) {
                        final XLinkToolView view = (XLinkToolView) li.getModelObject();
                        li.add(new Label("viewName", view.getName()));
                        li.add(new Label("viewDescription", 
                                returnLocalizedDescription(view.getDescriptions())));                              
                        final XLinkModelInformation destModelInfo 
                            = chooserLogic.getModelClassOfView(hostId, tool.getId(), view.getViewId());
                        Link viewLink = new Link("viewLink") {
                            public void onClick() {
                                String sourceModelClass = modelId;   
                                XLinkMock.transformAndOpenMatch(sourceModelClass, 
                                        versionId, identifierValues, destModelInfo.getClassName(), 
                                        destModelInfo.getVersion(), tool.getId(), 
                                        view.getViewId());
                                handleSuccessResponse(resp);
                            }
                        };
                        if (XLinkMock.isTransformationPossible(modelId, versionId, destModelInfo.getClassName(), 
                                destModelInfo.getVersion())) {
                            String labelText = new StringResourceModel("toolchooser.match", this, null).getString();
                            viewLink.add(new Label("viewLinkLabel", labelText));                  
                        } else {
                            viewLink.setEnabled(false);
                            String labelText = new StringResourceModel("toolchooser.nomatch", this, null).getString();
                            viewLink.add(new Label("viewLinkLabel", labelText));       
                        }
                        li.add(viewLink);  
                    }
                };
                item.add(viewList);
            }
        };
        add(toolList);        
    }
    
    private void setContextFromId() {
        ContextHolder.get().setCurrentContextId(contextId);
    }
    
    private void setLocale(Locale locale) {
        if (locale != null) {
            getSession().setLocale(locale);
        }
    }    
    
    private String returnLocalizedDescription(Map<String, String> descriptions) {
        if (descriptions.isEmpty()) {
            return null;
        }
        if (!descriptions.containsKey(getLocaleKey())) {
            return descriptions.values().iterator().next();
        }
        return descriptions.get(getLocaleKey());
    }
    
    private String getLocaleKey() {
        return getLocale().getLanguage();
    }
    
}
