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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.xlink.exceptions.DomainNotLinkableException;
import org.openengsb.core.api.xlink.exceptions.OpenXLinkException;
import org.openengsb.core.api.xlink.model.XLinkConnector;
import org.openengsb.core.api.xlink.model.XLinkConnectorView;
import org.openengsb.core.api.xlink.model.XLinkConstants;
import org.openengsb.core.api.xlink.service.ui.ToolChooserLogic;
import org.openengsb.core.api.xlink.service.ui.XLinkMock;
import org.openengsb.core.services.xlink.XLinkUtils;
import org.openengsb.core.util.ModelUtils;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

/**
 * Manages the processing of an incoming XLink and, if necessary, renders a page
 * where the user can choose between available Views for XLinking.
 * TODO [OPENENGSB-3267] Hook filter onto this WebPage
 */
@PaxWicketMountPoint(mountPoint = "openXLink")
public class ToolChooserPage extends WebPage {
    
    @PaxWicketBean(name = "osgiUtilsService")
    private OsgiUtilsService serviceUtils;
    
    @PaxWicketBean(name = "toolChooserLogic")
    private ToolChooserLogic toolChooserLogic;
    
    @PaxWicketBean(name = "xLinkMock")
    private XLinkMock xLinkMock;
    
    private String contextId;
    private String modelId;
    private String versionId;
    private Calendar expirationDate;
    private String hostId;
    private String identifier;
    private Object identifierObject;
    private String connectorId;
    private String viewId;
    
    private Map<String, String[]> requestParameters;
    
    public ToolChooserPage() {
        processPage();
    }
    
    
    public ToolChooserPage(PageParameters parameters) {
        processPage();
    }
    
    /**
     * Validates the incoming request and manages the further processing of the XLink.
     */
    private void processPage() {      
        requestParameters = getRequestParametersAsAMap();
        HttpServletRequest req = (HttpServletRequest) getRequest().getContainerRequest();
        HttpServletResponse resp = (HttpServletResponse) getResponse().getContainerResponse();
        setContextFromId();
        try {
            checkIfXLinkIsValid(req);
        } catch (OpenXLinkException ex) {
            handleErrorResponse(ex.getMessage());
        }
        if (checkForLocalSwitchingParameters()) {           
            ModelDescription destinationModelClass = toolChooserLogic.getModelClassOfView(hostId, connectorId, viewId);
            triggerXLinkProcessing(destinationModelClass, connectorId, viewId);
            handleSuccessResponse(resp);
            return;
        }
        buildToolChooserPage(resp);      
    }
    
    /**
     * Returns the Requestparameter as a Map. 
     */
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
    
    /**
     * Returns the value of the given key from the parameterMap or null.
     */
    private String getParameterFromMap(String key) {
        return requestParameters.get(key) == null ? null : requestParameters.get(key)[0];
    }
    
    /**
     * Throws an OpenXLinkException, if the calling request was not a well-formed XLink,
     * does not contain the mandatory xlink parameters or does not contain the necessary IdentifyingFields
     * to the defined ModelClass.
     */
    private void checkIfXLinkIsValid(HttpServletRequest req) throws OpenXLinkException {
        fetchXLinkParameters(req);
        checkMandatoryXLinkParameters();
        if (checkForLocalSwitchingParameters()) {
            checkConnectorAndViewExists();
        }
        checkXLinkIsExpired();
        List<String> keyFields = null;
        try {
            keyFields = xLinkMock.getModelIdentifierToModelDescription(modelId, versionId);
        } catch (ClassNotFoundException ex) {
            String errorMsg = new StringResourceModel("error.modelClass.notfound", this, null).getString();
            Logger.getLogger(ToolChooserPage.class.getName()).log(Level.SEVERE, null, ex);
            throw new OpenXLinkException(String.format(errorMsg, ex.getMessage()));
        }
        fetchAndCheckIdentifier(keyFields);
    }
    
    /**
     * Fetches the mandatory parameters for XLink from the calling request.
     */
    private void fetchXLinkParameters(HttpServletRequest req) {
        contextId = getParameterFromMap(XLinkConstants.XLINK_CONTEXTID_KEY);
        modelId = getParameterFromMap(XLinkConstants.XLINK_MODELCLASS_KEY);
        versionId = getParameterFromMap(XLinkConstants.XLINK_VERSION_KEY);
        viewId = getParameterFromMap(XLinkConstants.XLINK_VIEW_KEY);
        identifier = getParameterFromMap(XLinkConstants.XLINK_IDENTIFIER_KEY);
        expirationDate = XLinkUtils.dateStringToCalendar(getParameterFromMap(XLinkConstants.XLINK_EXPIRATIONDATE_KEY));
        hostId = req.getRemoteAddr();
        connectorId = getParameterFromMap(XLinkConstants.XLINK_CONNECTORID_KEY);
    }    

    /**
     * Throws an OpenXLinkException, if one or more mandatory parameter for XLink where not supplied.
     */
    private void checkMandatoryXLinkParameters() throws OpenXLinkException {
        String errorMsg = new StringResourceModel("error.missingMandatoryGetParam", this, null).getString();
        if (contextId == null) { errorMsg += " " + XLinkConstants.XLINK_CONTEXTID_KEY; }
        if (modelId == null) { errorMsg += ", " + XLinkConstants.XLINK_MODELCLASS_KEY; }
        if (versionId == null) { errorMsg += ", " + XLinkConstants.XLINK_VERSION_KEY; }
        if (expirationDate == null) { errorMsg += ", " + XLinkConstants.XLINK_EXPIRATIONDATE_KEY; }
        if (hostId == null) { errorMsg += ", " + XLinkConstants.XLINK_HOST_HEADERNAME; }
        if (identifier == null) { errorMsg += ", " + XLinkConstants.XLINK_IDENTIFIER_KEY; }
        if ((contextId == null) 
                || (modelId == null) 
                || (versionId == null) 
                || (expirationDate == null) 
                || (hostId == null)
                || (identifier == null)) {
            throw new OpenXLinkException(errorMsg);
        }
    }    
    
    /**
     * Throws an OpenXLinkException, if the calling XLink has expired.
     */
    private void checkXLinkIsExpired() throws OpenXLinkException {
        if (expirationDate != null ? Calendar.getInstance().after(expirationDate) : false) {
            String expiredMsg = new StringResourceModel("error.xlinkHasExpired", this, null).getString();
            throw new OpenXLinkException(expiredMsg);
        }        
    }  
    
    /**
     * Throws an OpenXLinkException, if the supplied connector or viewId are registered for XLink
     * or do not exist.
     */
    private void checkConnectorAndViewExists() throws OpenXLinkException {
        String errorConnectorNotRegistered 
            = new StringResourceModel("error.connectorNotRegistrated", this, null).getString();
        String errorViewNotExisting 
            = new StringResourceModel("error.viewNotExisting", this, null).getString();
        if (!toolChooserLogic.isConnectorRegistrated(hostId, connectorId)) {
            throw new OpenXLinkException(errorConnectorNotRegistered);
        }
        if (!toolChooserLogic.isViewExisting(hostId, connectorId, viewId)) {
            throw new OpenXLinkException(errorViewNotExisting);
        }
    }
    
    /**
     * Fetches and validates the Paramters from the request, to the defined IdentifyingFields.
     */
    private void fetchAndCheckIdentifier(List<String> identifierKeyNames) throws OpenXLinkException {      
        Class clazz;
        try {
            clazz = XLinkUtils.getClassOfOpenEngSBModel(modelId, versionId, serviceUtils);
        } catch (ClassNotFoundException ex) {
            String errorMsg = new StringResourceModel("error.modelClass.notfound", this, null).getString();
            throw new OpenXLinkException(errorMsg);
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            identifierObject = mapper.readValue(identifier, clazz);
        } catch (Exception ex) {
            String errorMsg = new StringResourceModel("error.identifierIsNotValid", this, null).getString();
            throw new OpenXLinkException(errorMsg);
        }            
        boolean found;
        for (String key : identifierKeyNames) {
            found = false;
            for (OpenEngSBModelEntry entry : ModelUtils.getOpenEngSBModelEntries(identifierObject)) {
                if (entry.getKey().equals(key)) {
                    found = true;
                    if (entry.getValue() == null) {
                        String errorMsg = new StringResourceModel("error.missingIdentifier", this, null).getString();
                        throw new OpenXLinkException(String.format(errorMsg, entry.getKey()));
                    }
                }
            }
            if (!found) {
                String errorMsg = new StringResourceModel("error.missingIdentifyingField", this, null).getString();
                throw new OpenXLinkException(String.format(errorMsg, key));
            }
        }
    }
    
    /**
     * Returns true, if the calling XLink contains the nessecary parameters for 
     * 'local-switching'.
     */
    private boolean checkForLocalSwitchingParameters() {
        return (connectorId != null) && (viewId != null);
    }
    
    /**
     * Redirects the Request to the Page, that renders the Errormessage to a xlink call.
     */
    private void handleErrorResponse(String error) {
        if (checkForLocalSwitchingParameters()) {    
            throw new RestartResponseException(new MachineResponsePage(error, false));  
        } else {   
            String hostIdMsg = new StringResourceModel("hostId.info", this, null).getString();
            hostIdMsg = String.format(hostIdMsg, hostId);              
            throw new RestartResponseException(new UserResponsePage(error, hostIdMsg, true));        
        }
    }
    
    /**
     * Redirects the Request to the Page, that renders the Successmessage to a xlink call.
     */
    private void handleSuccessResponse(HttpServletResponse resp) {
        if (checkForLocalSwitchingParameters()) {
            String successMsg = new StringResourceModel("success.localSwitch", this, null).getString();
            throw new RestartResponseException(new MachineResponsePage(successMsg, true));
        } else {         
            String successMsg = new StringResourceModel("success.normalSwitch", this, null).getString();
            String hostIdMsg = new StringResourceModel("hostId.info", this, null).getString();
            hostIdMsg = String.format(hostIdMsg, hostId);            
            throw new RestartResponseException(new UserResponsePage(successMsg, hostIdMsg, false));                  
        }        

     
    }
    
    /**
     * Builds the Page where the user can choose between the available Views for XLinking.
     */
    private void buildToolChooserPage(final HttpServletResponse resp) {
        String hostIdMsg = new StringResourceModel("hostId.info", this, null).getString();
        hostIdMsg = String.format(hostIdMsg, hostId);
        add(new Label("hostId", hostIdMsg));
        List<XLinkConnector> tools = toolChooserLogic.getRegisteredToolsFromHost(hostId);    
        ListView toolList = new ListView("toolList", tools) {
            protected void populateItem(ListItem item) {
                final XLinkConnector tool = (XLinkConnector) item.getModelObject();
                item.add(new Label("toolName", tool.getToolName()));
                ListView viewList = new ListView("viewList", Arrays.asList(tool.getAvailableViews())) {
                    @Override
                    protected void populateItem(ListItem li) {
                        final XLinkConnectorView view = (XLinkConnectorView) li.getModelObject();
                        li.add(new Label("viewName", view.getName()));
                        li.add(new Label("viewDescription", 
                                returnLocalizedDescription(view.getDescriptions())));                              
                        final ModelDescription destModelInfo 
                            = toolChooserLogic.getModelClassOfView(hostId, tool.getId(), view.getViewId());
                        Link viewLink = new Link("viewLink") {
                            public void onClick() {
                                triggerXLinkProcessing(destModelInfo, tool.getId(), view.getViewId());
                                handleSuccessResponse(resp);
                            }
                        };
                        if (xLinkMock.isTransformationPossible(modelId, versionId, destModelInfo.getModelClassName(), 
                                destModelInfo.getVersionString())) {
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
    
    /**
     * Triggers the processing of the supplied XLink
     */
    private void triggerXLinkProcessing(ModelDescription destModelInfo,
            String connectorId, String viewId) {
        List<Object> modelObjectsDestination = new ArrayList<Object>();
        try {
            modelObjectsDestination = xLinkMock.transformModelObject(
                    modelId, 
                    versionId,
                    destModelInfo.getModelClassName(),
                    destModelInfo.getVersionString(),
                    identifierObject);
        } catch (ClassNotFoundException ex) {
            String errorMsg = new StringResourceModel("error.modelClass.notfound", this, null).getString();
            handleErrorResponse(String.format(errorMsg, ex.getMessage()));
        } catch (OpenXLinkException ex) {
            String errorMsg = new StringResourceModel("error.transformationNotPossible", this, null).getString();
            handleErrorResponse(String.format(errorMsg, ex.getMessage()));                                
        }
        try {
            if (!modelObjectsDestination.isEmpty()) {
                xLinkMock.openPotentialMatches(modelObjectsDestination, connectorId, viewId);
            }                   
        } catch (OsgiServiceNotAvailableException ex) {             
            String errorMsg = new StringResourceModel("error.connectorNotFound", this, null).getString();
            handleErrorResponse(errorMsg);
        } catch (DomainNotLinkableException ex) {
            String errorMsg = new StringResourceModel("error.connectorNotLinkable", this, null).getString();
            handleErrorResponse(errorMsg);
        } catch (OpenXLinkException ex) {
            String errorMsg = new StringResourceModel("error.connectorNotFound", this, null).getString();
            handleErrorResponse(errorMsg);
        }        
    }
    
    private void setContextFromId() {
        ContextHolder.get().setCurrentContextId(contextId);
    }
    
    private void setLocale(Locale locale) {
        if (locale != null) {
            getSession().setLocale(locale);
        }
    }    
    
    /**
     * Returns the description for the currently configured locale. 
     * If no description could be found for the configured locale, the first one
     * from the iterator is returned.
     */
    private String returnLocalizedDescription(Map<String, String> descriptions) {
        if (descriptions.isEmpty()) {
            return null;
        }
        if (!descriptions.containsKey(getLocaleKey())) {
            return descriptions.values().iterator().next();
        }
        return descriptions.get(getLocaleKey());
    }
    
    /**
     * Returns the currently configured locale key.
     */
    private String getLocaleKey() {
        return getLocale().getLanguage();
    }

    public void setServiceUtils(OsgiUtilsService serviceUtils) {
        this.serviceUtils = serviceUtils;
    } 
    
}
