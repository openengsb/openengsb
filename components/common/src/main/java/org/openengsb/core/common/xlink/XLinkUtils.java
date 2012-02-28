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

import org.openengsb.core.api.xlink.XLinkTemplate;

// @extract-start XLinkUtils
/**
 * Static util class for xlink, defining XLink keyNames and Examplemethods. Demonstrates how XLinkTemplates are prepared
 * and how valid XLink-Urls are generated.
 */
public final class XLinkUtils {
    
    private XLinkUtils() { 
    }

    /** Keyname of the ModelId */
    public static final String XLINK_MODELID_KEY = "modelId";

    /** Keyname of the ExpirationDate */
    public static final String XLINK_EXPIRATIONDATE_KEY = "expirationDate";

    /**
     * Demonstrates how a XLinkTemplate is prepared before it is transmitted to the client. Every baseUrl must contain
     * the modelId and the expirationDate as GET-Paramters.
     */
    public static XLinkTemplate prepareXLinkTemplate(String servletUrl, String modelId, List<String> keyNames,
            int expirationDays) {
        servletUrl +=
            "?" + XLINK_MODELID_KEY + "=" + modelId + "&" + XLINK_EXPIRATIONDATE_KEY + "="
                    + getExpirationDate(expirationDays);
        return new XLinkTemplate(servletUrl, keyNames);
    }

    /**
     * Demonstrates how a valid XLink-Url is generated out of an XLinkTemplate and a List of values, corresponding to
     * the List of keyNames of the Template
     */
    public static String generateValidXLinkUrl(XLinkTemplate template, List<String> values) {
        String completeUrl = template.getBaseUrl();
        List<String> keyNames = template.getKeyNames();
        for (int i = 0; i < keyNames.size(); i++) {
            completeUrl += "&" + keyNames.get(i) + "=" + values.get(i);
        }
        return completeUrl;
    }

    /**
     * Returns a future Date-String in the format 'yyyyMMddkkmmss'.
     */
    private static String getExpirationDate(int futureDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, futureDays);
        Format formatter = new SimpleDateFormat("yyyyMMddkkmmss");
        return formatter.format(calendar.getTime());
    }

}
// @extract-end
