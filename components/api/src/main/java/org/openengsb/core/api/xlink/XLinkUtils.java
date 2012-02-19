/*
 *  Copyright (C) 2012 Christoph Prybila <christoph@prybila.at>
 * 
 *  This file is part of TMSimulator.
 *  A program created to demonstrate Turing Machines.
 * 
 *  TMSimulator is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  TMSimulator is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser Public License
 *  along with TMSimulator.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.openengsb.core.api.xlink;

/**
 * Static util class for xlink, containing utilFunctions and xlink definition fields.
 * Creation Date: 19.02.2012
 * @author Christoph Prybila <christoph@prybila.at>
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
     * Returns the IdentifierId as IdentifierField
     * @param identifierId to wrap into a field
     * @return the IdentifierId as IdentifierField
     */
    public static XLinkIdentifierField returnIdentifierIdAsField(Object identifierId){
        return new XLinkIdentifierField(XLINK_IDENTIFIER_KEY, "Id of XLinkIdentifier Instance.", identifierId, identifierId.getClass(), true);
    }
}
