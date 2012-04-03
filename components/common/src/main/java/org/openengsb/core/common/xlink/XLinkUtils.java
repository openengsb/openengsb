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
import java.util.List;

import org.openengsb.core.api.xlink.XLinkRegisteredTools;
import org.openengsb.core.api.xlink.XLinkTemplate;

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

    /** Keyname of the ModelId, mandatoryGET-Parameter in XLinks */
    public static final String XLINK_MODELID_KEY = "modelId";

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
    
    private static final String DateFormat = "yyyyMMddkkmmss";

    // @extract-end

    // @extract-start XLinkUtilsPrepareTemplate

    /**
     * Demonstrates how the baseUrl of a XLinkTemplate is prepared before it is transmitted to the client. Every baseUrl
     * must contain the contextId, modelId, itÂ´s version and the expirationDate as GET-Paramters, before it is
     * transmited to the connector. The ConnectorId-Key and the ViewId-Key are also added to the Template to enable
     * Local Switching.
     */
    public static XLinkTemplate prepareXLinkTemplate(String servletUrl, String contextId, String version,
            String modelId, List<String> keyNames, int expirationDays, List<XLinkRegisteredTools> registeredTools) {
        servletUrl +=
            "?" + XLINK_CONTEXTID_KEY + "=" + contextId + "&" + XLINK_VERSION_KEY + "=" + version + "&"
                    + XLINK_MODELID_KEY + "=" + modelId + "&" + XLINK_EXPIRATIONDATE_KEY + "="
                    + getExpirationDate(expirationDays);
        return new XLinkTemplate(servletUrl, keyNames, registeredTools, XLINK_CONNECTORID_KEY, XLINK_VIEW_KEY);
    }

    /**
     * Returns a future Date-String in the format 'yyyyMMddkkmmss'.
     */
    private static String getExpirationDate(int futureDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, futureDays);
        Format formatter = new SimpleDateFormat(DateFormat);
        return formatter.format(calendar.getTime());
    }

    // @extract-end

    // @extract-start XLinkUtilsGenerateValidXLinkUrl
    /**
     * Demonstrates how a valid XLink-Url is generated out of an XLinkTemplate and a List of values, corresponding to
     * the List of keyNames of the Template. Depending on the contained Keys, the XLink is useable for local switching,
     * or not.
     */
    public static String generateValidXLinkUrl(XLinkTemplate template, List<String> values) {
        String completeUrl = template.getBaseUrl();
        List<String> keyNames = template.getKeyNames();
        for (int i = 0; i < keyNames.size(); i++) {
            completeUrl += "&" + keyNames.get(i) + "=" + values.get(i);
        }
        return completeUrl;
    }

    // @extract-end

    // @extract-start XLinkUtilsGenerateValidXLinkUrlForLocalSwitching
    /**
     * Demonstrates how a valid XLink-Url for a Local Switching is generated out of an XLinkTemplate, a List of values
     * corresponding to the List of keyNames of the Template and the ConnectorId and the ViewId.
     */
    public static String generateValidXLinkUrlForLocalSwitching(XLinkTemplate template, List<String> values,
            String connectorIdValue, String viewIdValue) {
        String xLink = generateValidXLinkUrl(template, values);
        xLink +=
            "&" + template.getConnectorIdKeyName() + "=" + connectorIdValue + "&" + template.getViewIdKeyName() + "="
                    + viewIdValue;
        return xLink;
    }

    // @extract-end
    
    public static Calendar dateStringToCalendar(String dateString){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(DateFormat);
        try {
            calendar.setTime(formatter.parse(dateString));
        } catch (Exception ex) {
            return null;
        }
        return calendar;
    }

}
