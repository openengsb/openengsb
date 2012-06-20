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

package org.openengsb.core.common.xlink;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.core.api.ekb.ModelRegistry;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelWrapper;
import org.openengsb.core.api.xlink.model.XLinkLocalTool;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolRegistration;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.openengsb.core.common.util.ModelUtils;
import org.osgi.framework.Version;

/**
 * Static util class for xlink, defining XLink keyNames and Examplemethods. Demonstrates how XLinkTemplates are prepared
 * and how valid XLink-Urls are generated.
 */
public final class XLinkUtils {

    private XLinkUtils() {
    }

    // @extract-start XLinkUtilsKeyDefs

    /** Keyname of the ProjectId, mandatory GET-Parameter in XLinks */
    public static final String XLINK_CONTEXTID_KEY = "contextId";

    /** Keyname of the ModelClass, mandatoryGET-Parameter in XLinks */
    public static final String XLINK_MODELCLASS_KEY = "modelClass";

    /** Keyname of the Version, mandatory GET-Parameter in XLinks */
    public static final String XLINK_VERSION_KEY = "versionId";

    /** Keyname of the ExpirationDate, mandatory GET-Parameter in XLinks */
    public static final String XLINK_EXPIRATIONDATE_KEY = "expirationDate";
    
    /** Keyname of the IdentifierString, mandatory GET-Parameter in XLinks */
    public static final String XLINK_IDENTIFIER_KEY = "identifier";    

    /** Keyname of the ConnectorId, GET-Parameter in XLinks, only mandatory in local switching */
    public static final String XLINK_CONNECTORID_KEY = "connectorId";

    /** Keyname of the ViewId, GET-Parameter in XLinks, only mandatory in local switching */
    public static final String XLINK_VIEW_KEY = "viewId";

    /** Headername of the HostId (e.g. the IP), used during the registration for XLink. */
    public static final String XLINK_HOST_HEADERNAME = "Host";
    
    /**Format of the ExpirationDate*/
    public static final String DATEFORMAT = "yyyyMMddkkmmss";

    // @extract-end

    // @extract-start XLinkUtilsPrepareTemplate

    /**
     * Demonstrates how the baseUrl of a XLinkTemplate may be prepared before it is transmitted to the client. 
     * Every baseUrl must contain the expirationDate as a GET-Paramter, before it is 
     * transmited to the connector. The models are naively assigned to the views.
     * The contextIdKeyName is added to the template, must be set by the tool to 
     * determine the OpenEngSB context of the XLink
     * The ConnectorId/value combination and the ViewId-Key are also added to the Template to enable 
     * Local Switching.
     */
    public static XLinkTemplate prepareXLinkTemplate(String baseUrl,
            String connectorId,
            Map<ModelDescription, List<XLinkToolView>> modelsToViews, 
            int expirationDays, 
            List<XLinkLocalTool> registeredTools) {
        baseUrl +=
            "?" + XLINK_EXPIRATIONDATE_KEY + "=" + urlEncodeParameter(getExpirationDate(expirationDays));
        String connectorIdParam = XLINK_CONNECTORID_KEY + "=" + urlEncodeParameter(connectorId);
        Map<String, ModelDescription> viewToModels = assigneModelsToViews(modelsToViews);
        return new XLinkTemplate(baseUrl, 
                viewToModels, 
                XLINK_MODELCLASS_KEY,
                XLINK_VERSION_KEY,
                registeredTools,
                XLINK_CONTEXTID_KEY, 
                connectorIdParam, 
                XLINK_VIEW_KEY,
                XLINK_IDENTIFIER_KEY);
    }
    
    /**
     * Naive model to view assignment. 
     * Current model is choosen for the first occurence of the view.
     */
    private static Map<String, ModelDescription> assigneModelsToViews(Map<ModelDescription, 
            List<XLinkToolView>> modelsToViews) {
        HashMap<String, ModelDescription> viewsToModels = new HashMap<String, ModelDescription>();
        for (ModelDescription modelInfo : modelsToViews.keySet()) {
            List<XLinkToolView> currentViewList = modelsToViews.get(modelInfo);
            for (XLinkToolView view : currentViewList) {
                if (!viewsToModels.containsKey(view.getViewId())) {
                    viewsToModels.put(view.getViewId(), modelInfo);
                }
            }
        }
        return viewsToModels;
    }

    /**
     * Returns a future Date-String in the format 'yyyyMMddkkmmss'.
     */
    private static String getExpirationDate(int futureDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, futureDays);
        Format formatter = new SimpleDateFormat(DATEFORMAT);
        return formatter.format(calendar.getTime());
    }

    // @extract-end

    // @extract-start XLinkUtilsGenerateValidXLinkUrl
    /**
     * Demonstrates how a valid XLink-Url is generated out of an XLinkTemplate, the Modelclass and a List of values, 
     * corresponding to the List of keyFields of the Modelclass. 
     * Depending on the contained Keys, the XLink is useable for local switching, or not.
     */
    public static String generateValidXLinkUrl(XLinkTemplate template, 
            List<Object> identifierValues, 
            ModelDescription modelInformation, 
            String contextId,
            OsgiUtilsService serviceFinder) throws ClassNotFoundException, IOException {
        String completeUrl = template.getBaseUrl();    
        completeUrl += "&" + template.getModelClassKey() + "=" + urlEncodeParameter(modelInformation.getModelClassName());
        completeUrl += "&" + template.getModelVersionKey() + "=" + urlEncodeParameter(modelInformation.getModelVersionString());
        completeUrl += "&" + template.getContextIdKeyName() + "=" + urlEncodeParameter(contextId);        
        OpenEngSBModel modelOfView = createInstanceOfModelClass(
                modelInformation.getModelClassName(), modelInformation.getModelVersionString(), serviceFinder);
        List<OpenEngSBModelEntry> keyNames = modelOfView.getOpenEngSBModelEntries();
        for (int i = 0; i < keyNames.size(); i++) {
            modelOfView.getOpenEngSBModelEntries().get(i).setValue(identifierValues.get(i));
        } 
        
        OpenEngSBModelWrapper wrapper = ModelUtils.generateWrapperOutOfModel(modelOfView);
        ObjectMapper mapper = new ObjectMapper();
        String jsonWrapper = mapper.writeValueAsString(wrapper);
        completeUrl += "&" + template.getIdentifierKeyName() + "=" + urlEncodeParameter(jsonWrapper);
        return completeUrl;
    }

    // @extract-end
    
      
    public static OpenEngSBModel createInstanceOfModelClass(
            String clazz, 
            String version,
            OsgiUtilsService serviceFinder) throws ClassNotFoundException {
        ModelRegistry registry = serviceFinder.getService(ModelRegistry.class);
        Version versionObj = new Version(version);
        String versionString = versionObj.toString();
        ModelDescription modelDescription = new ModelDescription(clazz,versionObj);
        Class clazzDef = registry.loadModel(modelDescription);      
        return ModelUtils.createEmptyModelObject(clazzDef) ;
    }  

    // @extract-start XLinkUtilsGenerateValidXLinkUrlForLocalSwitching
    /**
     * Demonstrates how a valid XLink-Url is generated out of an XLinkTemplate, the Modelclass and a List of values, 
     * corresponding to the List of keyFields of the Modelclass. The connectorId and viewId parameters are added 
     * in the end, to mark the link for Local Switching
     */
    public static String generateValidXLinkUrlForLocalSwitching(XLinkTemplate template, List<Object> values,
            ModelDescription modelInformation, 
            String contextId, 
            String viewIdValue,
            OsgiUtilsService serviceFinder) throws ClassNotFoundException, IOException {
        String xLink = generateValidXLinkUrl(template, values, modelInformation, contextId, serviceFinder);
        xLink += "&" 
                + template.getConnectorId() + "&" 
                + template.getViewIdKeyName() + "=" + urlEncodeParameter(viewIdValue);
        return xLink;
    }

    // @extract-end
    
    public static Calendar dateStringToCalendar(String dateString) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(DATEFORMAT);
        try {
            calendar.setTime(formatter.parse(dateString));
        } catch (Exception ex) {
            return null;
        }
        return calendar;
    }
    
    private static String urlEncodeParameter(String parameter) {
        try {
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(XLinkUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parameter;
    }
    
    public static List<XLinkToolView> getViewsOfRegistration(XLinkToolRegistration registration) {
        List<XLinkToolView> viewsOfRegistration = new ArrayList<XLinkToolView>();
        Map<ModelDescription, List<XLinkToolView>> modelsToViews = registration.getModelsToViews();
        for (List<XLinkToolView> views : modelsToViews.values()) {
            for (XLinkToolView view : views) {
                if (!viewsOfRegistration.contains(view)) {
                    viewsOfRegistration.add(view);
                }
            }
        }
        return viewsOfRegistration;
    }
    
    public static List<XLinkLocalTool> getLocalToolFromRegistrations(List<XLinkToolRegistration> registrations) {
        List<XLinkLocalTool> tools = new ArrayList<XLinkLocalTool>();
        for (XLinkToolRegistration registration : registrations) {
            XLinkLocalTool newLocalTools 
                = new XLinkLocalTool(
                        registration.getConnectorId(), 
                        registration.getToolName(), 
                        getViewsOfRegistration(registration));
            tools.add(newLocalTools);
        }
        return tools;
    }    

}
