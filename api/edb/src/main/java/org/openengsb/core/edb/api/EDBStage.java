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
package org.openengsb.core.edb.api;

import java.io.Serializable;

/**
 * The EDBStage represents an stage and holds all needed information
 * for staging.
 */
public interface EDBStage extends Serializable {
    /**
     * Set the id of the current stage.
     */
    void setStageId(String id);
    
    /**
     * Get the current stage id.
     */
    String getStageId();
    
    /**
     * Set the creator of the current stage.
     */
    void setCreator(String creator);
    
    /**
     * Get the creator of the stage.
     */
    String getCreator();
    
    /**
     * Set the creation time of the current stage.
     */
    void setTimeStamp(Long timeStamp);
    
    /**
     * Get the creation time of the current stage.
     */
    Long getTimeStamp();
}
