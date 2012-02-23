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
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.xlink.XLinkIdentifier;
import org.openengsb.core.api.xlink.XLinkUrl;

/**
 * Static util class for xlink, containing xlink definition fields and methods for Linkcreation
 */
public class XLinkUtils {
    
    /**Keyname of the identifierId*/
    public static final String XLINK_IDENTIFIER_KEY = "identifierTemplateId";
    /**Keyname of the projectId*/
    public static final String XLINK_IDENTIFIER_PROJECT = "projectId";
    /**Keyname of the version*/
    public static final String XLINK_IDENTIFIER_VERSION = "versionId";    
    /**Keyname of the metadata*/
    public static final String XLINK_IDENTIFIER_METADATA = "metadataId";         
    
    /**
     * Return the Link to the Registry´s HTTP-Servlet containing the complete Modelobject´s Identifier. 
     * The fields of the Identifier are transportet as GET paramteters.
     * Every IdentifierField which is null is set with placeholder value like '$$KeyValue$$'. 
     * e.g. a field with the value null and the key 'Project' results in 'Project=$$Project$$'.
     * Returns null if the XLinkUrls attributes are not set.
     */
    public static String returnXLinkUrl(XLinkUrl xLinkUrl){
        String completeUrl = xLinkUrl.getUrl();       
        if(completeUrl == null)return null;
        List<OpenEngSBModelEntry> entries = xLinkUrl.getIdentifier().getOpenEngSBModelEntries();
        for(OpenEngSBModelEntry entry : entries){
            if(entry.getValue() == null && containsPartnerField(entries,entry)){
                continue;
            } 
            if(xLinkUrl.getUrl().equals(completeUrl)){
                completeUrl+="?";
            }else{
                completeUrl+="&";
            }      
            completeUrl += entry.getKey()+"=";
            if(entry.getValue() == null){
                completeUrl += "$$"+entry.getKey()+"$$";
            }else{
                completeUrl += entry.getValue();
            }
        }
        return completeUrl;
    }
    
    private static boolean containsPartnerField(List<OpenEngSBModelEntry> entries, OpenEngSBModelEntry entry){     
        String key = entry.getKey();
        String firstChar = key.substring(0, 1);
        if(firstChar.matches("[A-Z]")){
            firstChar = firstChar.toLowerCase();
        }else{
            firstChar = firstChar.toUpperCase();
        }
        String partnerKey = firstChar + key.substring(1,key.length());
        return containsKey(entries,partnerKey);
    }
    
    private static boolean containsKey(List<OpenEngSBModelEntry> entries, String key){
    	boolean containsKey = false;
    	for(OpenEngSBModelEntry entry : entries){
    		if(entry.getKey().equals(key)){
    			containsKey = true;
    			break;
    		}
    	}
    	return containsKey; 
    }
     
    public static String returnValidTillTimeStamp(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 3);
        Format formatter = new SimpleDateFormat("yyyyMMddkkmmss");
        return formatter.format(calendar.getTime());
    }
    
    /**
     * Returns true if the xLinkUrl´s identifier is duly completed
     * @return true if the xLinkUrl´s identifier is duly completed
     */
    public static boolean isUrlDulyCompleted(XLinkUrl xLinkUrl){
        return isIdentifierDulyCompleted(xLinkUrl.getIdentifier());
    }
    
    /**
     * Returns true if the XLink Identifier is duly completed
     * @return true if the XLink Identifier is duly completed
     */
    private static boolean isIdentifierDulyCompleted(XLinkIdentifier xLinkIdentifier){
        boolean ok = true;
        for(OpenEngSBModelEntry entry  : xLinkIdentifier.getOpenEngSBModelEntries()){
            if(entry.getValue() == null && !containsPartnerField(xLinkIdentifier.getOpenEngSBModelEntries(),entry)){
                ok = false;
                break;
            }
        }
        return ok;
    }    
    
}
