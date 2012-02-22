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

package org.openengsb.core.api.xlink;

import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * Modelclass for the XLinkURL definitions. To be used to transfere the Linkstrukture to the CLienttools.
 * May also be filled by actual values and automates the creation of valid XLinks.
 */
public interface XLinkUrl extends OpenEngSBModel{
    
    /**
     * ModelobjectIdentifier, containing the Fields to append to the URL as Parameters. 
     */
    public XLinkIdentifier getIdentifier();
    
    public void setIdentifier(XLinkIdentifier identifier);
    /**
     * URL to the Registry´s HTTP-Servlet without the Identifier Fields as Parameters
     */
    public String getUrl();

    public void setUrl(String url);
    
}
