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

package org.openengsb.core.services.xlink;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.xlink.model.XLinkConnector;
import org.openengsb.core.api.xlink.model.XLinkConnectorRegistration;
import org.openengsb.core.api.xlink.model.XLinkConnectorView;
import org.openengsb.core.api.xlink.model.XLinkConstants;
import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;
import org.openengsb.core.api.xlink.model.XLinkUrlKeyNames;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.util.ModelUtils;
import org.osgi.framework.Version;

/**
 * Static util class for xlink, defining XLink keyNames and UtilMethods. 
 * Provides the preparaition of XLinkTemplates and demonstrates how 
 * valid XLink-Urls are generated.
 */
public final class XLinkUtils {

    private XLinkUtils() {
    }

    // @extract-start XLinkUtilsPrepareTemplate

    /**
     * Prepares the XLinkTemplate before it is transmitted to the remote tool. 
     * <br/><br/>
     * The baseUrl is prepared and the KeyNames are added. <br/>
     * The models are assigned to the views (yet in an naive order). <br/>
     * The ConnectorId/value combination and the ViewId-Key are also added to the Template to enable 
     * Local Switching.
     */
    public static XLinkUrlBlueprint prepareXLinkTemplate(String baseUrl,
            String connectorId,
            Map<ModelDescription, XLinkConnectorView[]> modelsToViews, 
            int expirationDays, 
            XLinkConnector[] registeredTools) {
        baseUrl +=
            "?" + XLinkConstants.XLINK_EXPIRATIONDATE_KEY + "=" + urlEncodeParameter(getExpirationDate(expirationDays));
        String connectorIdParam = XLinkConstants.XLINK_CONNECTORID_KEY + "=" + urlEncodeParameter(connectorId);
        Map<String, ModelDescription> viewToModels = assigneModelsToViews(modelsToViews);
        return 
            new XLinkUrlBlueprint(baseUrl, 
                viewToModels, 
                registeredTools,        
                connectorIdParam, 
            new XLinkUrlKeyNames()
        );
    }
    
    /**
     * Naive model to view assignment. 
     * The current model is choosen for the first occurence of the view.
     */
    private static Map<String, ModelDescription> assigneModelsToViews(Map<ModelDescription, 
            XLinkConnectorView[]> modelsToViews) {
        HashMap<String, ModelDescription> viewsToModels = new HashMap<String, ModelDescription>();
        for (ModelDescription modelInfo : modelsToViews.keySet()) {
            List<XLinkConnectorView> currentViewList = Arrays.asList(modelsToViews.get(modelInfo));
            for (XLinkConnectorView view : currentViewList) {
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
        Format formatter = new SimpleDateFormat(XLinkConstants.DATEFORMAT);
        return formatter.format(calendar.getTime());
    }

    // @extract-end
    
    /**
     * Sets the value, of the field defined in the OpenEngSBModelEntry, the the given model, 
     * with reflection.
     */
    public static void setValueOfModel(Object model, OpenEngSBModelEntry entry, Object value) throws 
            NoSuchFieldException, 
            IllegalArgumentException, 
            IllegalAccessException {
        Class clazz = model.getClass();
        Field field = clazz.getDeclaredField(entry.getKey());
        field.setAccessible(true);
        field.set(model, value);    
    }
      
    /**
     * Returns an empty instance to a given Classobject.
     * Returns null, if an error happens during the instantiation.
     */
    public static Object createEmptyInstanceOfModelClass(Class clazzObject) {
        return createInstanceOfModelClass(clazzObject,
                new ArrayList<OpenEngSBModelEntry>());
    }  
    
    /**
     * Returns an instance to a given Classobject and a List of OpenEngSBModelEntries.
     * Returns null, if an error happens during the instantiation.
     */
    public static Object createInstanceOfModelClass(Class clazzObject,
            List<OpenEngSBModelEntry> entries) {
        return ModelUtils.createModel(clazzObject, entries);
    }      
    
    /**
     * Returns the Classobject to a given clazzString and a given version.
     * Throws a ClassNotFoundException if the Class/Version pair is not found.
     */
    public static Class getClassOfOpenEngSBModel(String clazz, 
            String version,
            OsgiUtilsService serviceFinder) throws ClassNotFoundException {
        ModelRegistry registry = serviceFinder.getService(ModelRegistry.class);
        Version versionObj = new Version(version);
        ModelDescription modelDescription = new ModelDescription(clazz, versionObj);
        Class clazzObject = registry.loadModel(modelDescription);   
        return clazzObject;
    }
    
    /**
     * Returns a Calendarobject to a given dateString in the Format 'DATEFORMAT'.
     * If the given String is of the wrong format, null is returned.
     * @see XLinkUtils#DATEFORMAT
     */
    public static Calendar dateStringToCalendar(String dateString) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(XLinkConstants.DATEFORMAT);
        try {
            calendar.setTime(formatter.parse(dateString));
        } catch (Exception ex) {
            return null;
        }
        return calendar;
    }
    
    /**
     * Encodes a given Parameter in UTF-8. 
     */
    private static String urlEncodeParameter(String parameter) {
        try {
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(XLinkUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parameter;
    }
    
    /**
     * Returns a distinct List of all RemoteToolViews, contained in a RemoteToolRegistration.
     * @see RemoteToolView
     * @see RemoteToolRegistration
     */
    public static XLinkConnectorView[] getViewsOfRegistration(XLinkConnectorRegistration registration) {
        List<XLinkConnectorView> viewsOfRegistration = new ArrayList<XLinkConnectorView>();
        Map<ModelDescription, XLinkConnectorView[]> modelsToViews = registration.getModelsToViews();
        for (XLinkConnectorView[] views : modelsToViews.values()) {
            for (int i = 0; i < views.length; i++) {
                XLinkConnectorView view = views[i];
                if (!viewsOfRegistration.contains(view)) {
                    viewsOfRegistration.add(view);
                }
            }                
        }
        return viewsOfRegistration.toArray(new XLinkConnectorView[0]);
    }
    
    /**
     * Returns a list of RemoteTools to a list of RemoteToolRegistrations.
     * The list of RemoteTool can be sent to a remote host. A RemoteToolRegistration
     * is for internal usage only.
     * @see RemoteToolRegistration
     * @see RemoteTool
     */
    public static XLinkConnector[] getLocalToolFromRegistrations(List<XLinkConnectorRegistration> registrations) {
        List<XLinkConnector> tools = new ArrayList<XLinkConnector>();
        for (XLinkConnectorRegistration registration : registrations) {
            XLinkConnector newLocalTools 
                = new XLinkConnector(
                        registration.getConnectorId(), 
                        registration.getToolName(), 
                        getViewsOfRegistration(registration));
            tools.add(newLocalTools);
        }
        return tools.toArray(new XLinkConnector[0]);
    }    

}
