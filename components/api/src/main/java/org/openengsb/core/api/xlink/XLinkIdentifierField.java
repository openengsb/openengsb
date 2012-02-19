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

/**
 * Modelclass for Field of XLink´s ModelIdentifier. Contains a Description, Class of Inputvalue and may hold the actual value.
 * Fields may be marked as 'Final', this enables the Registry to pre set values that are not be changed by the tools.
 * Creation Date: 19.02.2012
 * @author Christoph Prybila <christoph@prybila.at>
 */
public class XLinkIdentifierField {
    /**Key of IdentifierField*/
    private String key;
    /**Description of IdentifierField*/
    private String description;
    /**Value of IdentifierField, should be convertable to String*/
    private Object value;
    /**Class of the value of IdentifierField*/
    private Class<?> type;
    /**Flag indicating if the Field is complete and may not be changed*/
    private boolean finalField;

    public XLinkIdentifierField(String key, String description, Class<?> type) {
        this.key = key;
        this.description = description;
        this.type = type;
    }

    public XLinkIdentifierField(String key, String description, Object value, Class<?> type, boolean finalField) {
        this.key = key;
        this.description = description;
        this.type = type;
        this.value = value;
        this.finalField = finalField;
    }
    
    /**
     * Returns a Flag indicating if the Field is complete and may not be changed.
     * @return a Flag indicating if the Field is complete and may not be changed.
     */
    public boolean isFinalField() {
        return finalField;
    }
    
    /**
     * Returns the Description to the IdentifierField.
     * @return the Description to the IdentifierField.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the Key to the IdentifierField.
     * @return the Key to the IdentifierField.
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the Class of the value of the IdentifierField.
     * @return the Class of the value of the IdentifierField.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Sets the Value of the XLinkIdentifier.
     * @param value to set the Field to 
     * @throws XLinkFieldException This exception is thrown, if the Inputvalue to a XLinkIdentifierField is of the wrong Class or the Field is final.
     */
    public void setValue(Object value) throws XLinkFieldException{
        if(isFinalField())throw new XLinkFieldException("Field is Final, Value cannot be changed.");
        if(value.getClass() != type.getClass())throw new XLinkFieldException("Wrong class for inputvalue of Identifierfield. Class was "+value.getClass()+" but should be "+type);
        this.value = value;
    }
    
    /**
     * Returns the value of the IdentifierField.
     * @return the value of the IdentifierField.
     */
    public Object getValue() {
        return value;
    }
    
}
