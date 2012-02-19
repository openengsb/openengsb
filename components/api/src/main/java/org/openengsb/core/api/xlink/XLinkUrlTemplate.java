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

import org.openengsb.core.api.xlink.exception.XLinkFieldException;
import java.util.List;

/**
 * Modelclass for the XLink URLs Templatedefinitions. To be used to transfere the Linkstrukture and additional informations to the CLienttools.
 * May also be filled by actual values and automates the creation of valid XLinks.
 * Creation Date: 19.02.2012
 * @author Christoph Prybila <christoph@prybila.at>
 */
public class XLinkUrlTemplate {
    
    /**URL to the Registry´s HTTP-Servlet without the Identifier Fields as Parameters*/
    private String url;
    /**ModelobjectIdentifier, containing the Fields to append to the URL as Parameters. 
     * Every Field consists of a description and ooptional it´s value.*/
    private XLinkIdentifier modelObjectIdentifier;

    public XLinkUrlTemplate(String url, XLinkIdentifier modelObjectIdentifier) {
        this.url = url;
        this.modelObjectIdentifier = modelObjectIdentifier;
    }

    /**
     * Returns all Fields of the contained Identifier.
     * @return all Fields of the contained Identifier.
     */
    public List<XLinkIdentifierField> getIdentifierFields() {
        return modelObjectIdentifier.getFields();
    }
    
    /**
     * Returns all Fields of the contained Identifier that are not marked as Final.
     * @return all Fields of the contained Identifier that are not marked as Final.
     */    
    public List<XLinkIdentifierField> getIdentifierFieldsNotFinal() {
        return modelObjectIdentifier.getFieldsNotFinal();
    }
    
    /**
     * Returns the Identifier´s field corresponding to the given key. Returns null if no field could be found to the given key.
     * @param key of corresponding field of modelObjectIdentifier.
     * @return the Identifier´s field corresponding to the given key. Returns null if no field could be found to the given key.
     */
    public XLinkIdentifierField getIdentifierField(String key) {
        return modelObjectIdentifier.getField(key);
    }
    
    /**
     * Sets the XLinkIdentifierField to the given key with the given value.
     * @param key of field
     * @param value to set the Field to 
     * @throws XLinkFieldException This exception is thrown, if no XLinkIdentifierField was found in the Hashmap of a XLinkIdentifier, for a given key or, if the Inputvalue to a XLinkIdentifierField is of the wrong Class or, if the Field is final..
     */
    public void setIdentifierField(String key, Object value) throws XLinkFieldException{
        modelObjectIdentifier.setField(key, value);
    }

    /**
     * Returns the URL to the Registry´s HTTP-Servlet without the Identifier Fields as Parameters.
     * @return the URL to the Registry´s HTTP-Servlet without the Identifier Fields as Parameters. 
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Sets the URL to the Registry´s HTTP-Servlet. 
     * @param url to the Registry´s HTTP-Servlet
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Returns true if the ModelObjectIdentifier´s fields are duly completed
     * @return true if the ModelObjectIdentifier´s fields are duly completed
     */
    public boolean isDulyCompleted(){
        boolean ok = true;
        for(XLinkIdentifierField field : modelObjectIdentifier.getFields()){
            if(field.getValue() == null){
                ok = false;
                break;
            }
        }
        return ok;
    }
    
    /**
     * Returns the LinkTemplate as a String, consisting of the URL to the Registry´s HTTP-Servlet and the ModelobjectIdentifier´s Fields appended as Parameters. 
     * The values of the parameters are set with placeholders of the format '$$keyOfField$$'. 
     * @return the LinkTemplate as a String, consisting of the URL to the Registry´s HTTP-Servlet and the ModelobjectIdentifier´s Fields appended as Parameters. 
     */
    public String getXLinkUrlTemplate(){
        String xLinkUrl = url;
        for(XLinkIdentifierField field : modelObjectIdentifier.getFields()){
            String fieldString = "";
            if(xLinkUrl.equals(url)){
                fieldString+="?";
            }else{
                fieldString+="&";
            }
            if(!field.isFinalField()){
                fieldString+=field.getKey()+"=$$"+field.getKey()+"$$";
            }else{
                fieldString+=field.getKey()+"="+field.getValue();
            }
            xLinkUrl += fieldString;
        }
        return xLinkUrl;        
    }
    
    /**
     * Return the Link to the Registry´s HTTP-Servlet containing the complete Modelobject´s Identifier.
     * Returns null if the Identifier has not been duly completet.
     * @return the Link to the Registry´s HTTP-Servlet containing the complete Modelobject´s Identifier. 
     */
    public String getXLinkUrl(){
        if(!isDulyCompleted())return null;
        String xLinkUrl = url;
        for(XLinkIdentifierField field : modelObjectIdentifier.getFields()){
            String fieldString = "";
            if(xLinkUrl.equals(url)){
                fieldString+="?";
            }else{
                fieldString+="&";
            }
            fieldString+=field.getKey()+"="+field.getValue();
            xLinkUrl += fieldString;
        }
        return xLinkUrl;
    }
    
}
