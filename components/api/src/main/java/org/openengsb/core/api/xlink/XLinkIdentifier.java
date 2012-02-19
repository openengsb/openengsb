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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Modelclass for the XLinkModel Identifiers.
 * It is consisting of a List of FieldNames, including their description and optional their values.
 * Fields may be marked as 'Final', this enables the Registry to pre set values that are not be changed by the tools.
 * Creation Date: 19.02.2012
 * @author Christoph Prybila <christoph@prybila.at>
 */
public class XLinkIdentifier {
    /**Id of XLinkIdentifier Instance. Must be included in xlink-urls in order to fetch this instance.*/
    private UUID identifierId;
    /**Fields of the XLinkIdentifier, contains instances of XLinkIdentifierField with it´s key variable as HashMap keys.*/
    private HashMap<String,XLinkIdentifierField> fields;
    
    /**
     * Initializes the Identfier.
     * Generates and includes the IdentifierId in the XLinkIdentifierField HashMap, if XLINK_IDENTIFIER_KEY was not contained in the inputCollection.
     * @see org.openengsb.core.api.xlink.XLinkUtils#XLINK_IDENTIFIER_KEY
     */
    public XLinkIdentifier(Collection<XLinkIdentifierField> fieldValues) {
        fields = new HashMap<String,XLinkIdentifierField>();
        for(XLinkIdentifierField fieldvalue : fieldValues){
            fields.put(fieldvalue.getKey(), fieldvalue);
        }
        testAndSetMandatoryFields();
    }
    
    /**
     * Generates and includes the IdentifierId in the XLinkIdentifierField HashMap, if XLINK_IDENTIFIER_KEY was not contained in the inputCollection.
     * @see org.openengsb.core.api.xlink.XLinkUtils#XLINK_IDENTIFIER_KEY
     */
    private void testAndSetMandatoryFields(){        
        Object id = fields.get(XLinkUtils.XLINK_IDENTIFIER_KEY);
        if(id != null){
            this.identifierId = (UUID) id;
        }else{
            this.identifierId = UUID.randomUUID();
            fields.put(XLinkUtils.XLINK_IDENTIFIER_KEY, XLinkUtils.returnIdentifierIdAsField(identifierId));
        }
    }

    /**
     * Returns all XLinkIdentifierFields of the Identifier as a List.
     * @return all XLinkIdentifierFields of the Identifier as a List. 
     */
    public List<XLinkIdentifierField> getFields() {
        return new ArrayList(fields.values());
    }
    
    /**
     * Returns the XLinkIdentifierFields of the Identifier that are not marked as final as a List.
     * @return the XLinkIdentifierFields of the Identifier that are not marked as final as a List.
     */    
    public List<XLinkIdentifierField> getFieldsNotFinal(){
        List<XLinkIdentifierField> fieldList = new ArrayList<XLinkIdentifierField>();
        for(XLinkIdentifierField field : fields.values()){      
            if(!field.isFinalField())fieldList.add(field);
        }  
        return fieldList;
    }
    
    /**
     * Returns the XLinkIdentifierField corresponding to the given key. Returns null if no field could be found to the given key.
     * @param key of corresponding field of XLinkIdentifier
     * @return the XLinkIdentifierField corresponding to the given key. Returns null if no field could be found to the given key.
     */
    public XLinkIdentifierField getField(String key){
        return fields.get(key);
    }
    
    /**
     * Sets a Field of the XLinkIdentifier.
     * @param key of corresponding field of XLinkIdentifier
     * @param value to set the Field to 
     * @throws XLinkFieldException This exception is thrown, if no XLinkIdentifierField was found in the Hashmap of a XLinkIdentifier, for a given key or, if the Inputvalue to a XLinkIdentifierField is of the wrong Class or, if the Field is final..
     */
    public void setField(String key, Object value) throws XLinkFieldException{
        XLinkIdentifierField field = getField(key);
        if(field == null) throw new XLinkFieldException("No Identifierfield was found for key "+key);
        field.setValue(value);
    }

    /**
     * Returns the Id of the XLinkIdentifier instance
     * @return the Id of the XLinkIdentifier instance
     */
    public UUID getIdentifierId() {
        return identifierId;
    }    

}
