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

package org.openengsb.core.api.xlink.model;

/**
 * This class defines constants needed by XLink
 */
public final class XLinkConstants {
    
    private XLinkConstants() {
        
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

    /** Name of the HostId (currently the IP), used to for identification
     * during the registration for XLink and the tool-chooser webpage. */
    public static final String XLINK_HOST_HEADERNAME = "IP";
    
    /**Format of the ExpirationDate*/
    public static final String DATEFORMAT = "yyyyMMddkkmmss";

    // @extract-end
}
