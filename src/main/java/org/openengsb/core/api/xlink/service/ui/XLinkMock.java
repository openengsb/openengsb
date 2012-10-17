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

package org.openengsb.core.api.xlink.service.ui;

import java.util.List;

import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.xlink.exceptions.DomainNotLinkableException;
import org.openengsb.core.api.xlink.exceptions.OpenXLinkException;

/**
 * This class mocks the unfinished xlink-functionality.
 * When finished, this functionality should be moved to ToolChooserLogic
 */
public interface XLinkMock {
    
    /**
     * Transforms the given ModelObject from it´s SourceClass to the defined DestinationModel.  
     */
    public List<Object> transformModelObject(String sourceModelClass, String sourceModelVersion,             
            String destinationModelClass, String destinationModelVersion, 
            Object modelObjectSource) throws ClassNotFoundException, OpenXLinkException;
    
    /**
     * Calls the given connector to process the list of transformed Objects as 
     * potential XLink matches.
     */
    public void openPotentialMatches(List<Object> modelObjectsDestination, 
            String connectorToCall, String viewToCall) throws OsgiServiceNotAvailableException, 
            ClassCastException, OpenXLinkException, DomainNotLinkableException; 
    
    /**
     * Returns true, if the transformation between the two defined ModelClasses is possible.
     */
    public boolean isTransformationPossible(String srcModelClass, String srcModelVersion, 
            String destModelClass, String destModelVersion);   
    
    /**
     * Returns the FieldNames of the given ModelDescription (e.g. ModelClass and ModelVersion) which
     * are marked as identifying Fields for XLink.
     */
    public List<String> getModelIdentifierToModelDescription(
            String modelId, String versionId) throws ClassNotFoundException;
    
    
}
