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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.xlink.model.XLinkLocalTool;
import org.openengsb.core.api.xlink.model.XLinkModelInformation;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.openengsb.core.common.util.ModelUtils;

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

    /** Keyname of the ConnectorId, GET-Parameter in XLinks, only mandatory in local switching */
    public static final String XLINK_CONNECTORID_KEY = "connectorId";

    /** Keyname of the ViewId, GET-Parameter in XLinks, only mandatory in local switching */
    public static final String XLINK_VIEW_KEY = "viewId";

    /** Headername of the HostId (e.g. the IP), used during the registration for XLink. */
    public static final String XLINK_HOST_HEADERNAME = "Host";
    
    /**Format of the ExpirationDate*/
    private static final String DATEFORMAT = "yyyyMMddkkmmss";

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
            Map<XLinkModelInformation, List<XLinkToolView>> modelsToViews, 
            int expirationDays, 
            List<XLinkLocalTool> registeredTools) {
        baseUrl +=
            "?" + XLINK_EXPIRATIONDATE_KEY + "=" + getExpirationDate(expirationDays);
        String connectorIdParam = XLINK_CONNECTORID_KEY + "=" + connectorId;
        Map<String, XLinkModelInformation> viewToModels = assigneModelsToViews(modelsToViews);
        return new XLinkTemplate(baseUrl, 
                viewToModels, 
                XLINK_MODELCLASS_KEY,
                XLINK_VERSION_KEY,
                registeredTools,
                XLINK_CONTEXTID_KEY, 
                connectorIdParam, 
                XLINK_VIEW_KEY);
    }
    
    /**
     * Naive model to view assignment. 
     * Current model is choosen for the first occurence of the view.
     */
    private static Map<String, XLinkModelInformation> assigneModelsToViews(Map<XLinkModelInformation, 
            List<XLinkToolView>> modelsToViews) {
        HashMap<String, XLinkModelInformation> viewsToModels = new HashMap<String, XLinkModelInformation>();
        for (XLinkModelInformation modelInfo : modelsToViews.keySet()) {
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
            List<String> identifierValues, 
            XLinkModelInformation modelInformation, 
            String contextId) throws ClassNotFoundException {
        String completeUrl = template.getBaseUrl();
        completeUrl += "&" + template.getModelClassKey() + "=" + modelInformation.getClassName();
        completeUrl += "&" + template.getModelVersionKey() + "=" + modelInformation.getVersion();
        completeUrl += "&" + template.getContextIdKeyName() + "=" + contextId;        
        OpenEngSBModel modelOfView = createInstanceOfModelClass(modelInformation.getClassName());
        List<OpenEngSBModelEntry> keyNames = modelOfView.getOpenEngSBModelEntries();
        for (int i = 0; i < keyNames.size(); i++) {
            completeUrl += "&" + keyNames.get(i).getKey() + "=" + identifierValues.get(i);
        }
        return completeUrl;
    }

    // @extract-end
    
      
    private static OpenEngSBModel createInstanceOfModelClass(String clazz) throws ClassNotFoundException {
        return ModelUtils.createEmptyModelObject(ExampleObjectOrientedDomain.class) ;
    }  

    // @extract-start XLinkUtilsGenerateValidXLinkUrlForLocalSwitching
    /**
     * Demonstrates how a valid XLink-Url is generated out of an XLinkTemplate, the Modelclass and a List of values, 
     * corresponding to the List of keyFields of the Modelclass. The connectorId and viewId parameters are added 
     * in the end, to mark the link for Local Switching
     */
    public static String generateValidXLinkUrlForLocalSwitching(XLinkTemplate template, List<String> values,
            XLinkModelInformation modelInformation, 
            String contextId, 
            String viewIdValue) throws ClassNotFoundException {
        String xLink = generateValidXLinkUrl(template, values, modelInformation, contextId);
        xLink += "&" 
                + template.getConnectorId() + "&" 
                + template.getViewIdKeyName() + "=" + viewIdValue;
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

}