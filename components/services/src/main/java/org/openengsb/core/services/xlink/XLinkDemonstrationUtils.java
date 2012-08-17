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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.common.util.ModelUtils;

/**
 * Static util class for xlink, defining DemonstrationMethods. 
 */
public final class XLinkDemonstrationUtils {

    private XLinkDemonstrationUtils() {
    }
    
    // @extract-start XLinkUtilsGenerateValidXLinkUrl
    /**
     * For demonstration ONLY method.
     * <br/><br/>
     * Demonstrates how a valid XLink-Url is generated out of an XLinkTemplate, 
     * a ModelDescription and an Identifying Object, serialized with JSON.
     * This Method does not prepare the url for local switching.
     */
    public static String generateValidXLinkUrl(XLinkTemplate template, 
            ModelDescription modelInformation, 
            String contextId,
            String objectAsJsonString) {
        String completeUrl = template.getBaseUrl();    
        completeUrl += "&" + template.getKeyNames()
            .getModelClassKeyName() + "=" + urlEncodeParameter(modelInformation.getModelClassName());
        completeUrl += "&" + template.getKeyNames()
            .getModelVersionKeyName() + "=" + urlEncodeParameter(modelInformation.getVersionString());
        completeUrl += "&" + template.getKeyNames()
            .getContextIdKeyName() + "=" + urlEncodeParameter(contextId);   
        completeUrl += "&" + template.getKeyNames()
            .getIdentifierKeyName() + "=" + urlEncodeParameter(objectAsJsonString);
        return completeUrl;
    }

    // @extract-end
    
    /**
     * For demonstration ONLY method.
     * <br/><br/>
     * Fetches the class object to the given ModelDescription.
     * Creates an emtpy instance to the fetched Classobject and set List of values to
     * sequentially to the fields of the empty instance. 
     * Serializes this object to JSON and returns the String.
     */
    public static String serializeModelObjectToJSON(
            List<Object> identifierValues,
            ModelDescription modelInformation,
            OsgiUtilsService serviceFinder) throws ClassNotFoundException, 
            IOException, 
            NoSuchFieldException, 
            IllegalArgumentException, 
            IllegalAccessException {
        Class clazz = XLinkUtils.getClassOfOpenEngSBModel(modelInformation.getModelClassName(), 
            modelInformation.getVersionString(), serviceFinder);
        Object modelOfView = XLinkUtils.createEmptyInstanceOfModelClass(clazz);
        List<OpenEngSBModelEntry> keyNames = ModelUtils.getOpenEngSBModelEntries(modelOfView);
        for (int i = 0; i < keyNames.size(); i++) {
            XLinkUtils.setValueOfModel(modelOfView, keyNames.get(i), identifierValues.get(i));
        } 
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(modelOfView);        
    }    
    
    // @extract-start XLinkUtilsGenerateValidXLinkUrlForLocalSwitching    
    /**
     * For demonstration ONLY method.
     * <br/><br/>
     * Demonstrates how a valid XLink-Url is generated out of an XLinkTemplate, 
     * a ModelDescription and an Identifying Object, serialized with JSON.
     * The connectorId and viewId parameters are added, to mark the url for Local Switching
     */
    public static String generateValidXLinkUrlForLocalSwitching(XLinkTemplate template,
            ModelDescription modelInformation, 
            String contextId, 
            String viewIdValue,
            String objectAsJsonString) {
        String xLink = generateValidXLinkUrl(template, modelInformation, contextId, objectAsJsonString);
        xLink += "&" 
                + template.getConnectorId() + "&" 
                + template.getKeyNames().getViewIdKeyName() + "=" + urlEncodeParameter(viewIdValue);
        return xLink;
    }
    // @extract-end
    
    /**
     * Encodes a given Parameter in UTF-8. 
     */
    private static String urlEncodeParameter(String parameter) {
        try {
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(XLinkDemonstrationUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parameter;
    }

}
