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

package org.openengsb.domain.binarytransformation;

import java.io.File;
import java.util.List;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

/**
 * defines all needed functions that a binary transformation provider factory should have.
 */
public interface BinaryTransformationDomain extends Domain {

    /**
     * creates a new BinaryTransformationProvider under given binaryId which uses the transformation configs to convert
     * between the binary format and the OpenEngSBModelEntries
     */
    void register(String binaryId, Class<?> clasz, File... transformationConfigs);

    /**
     * deletes the BinaryTranforamtionProvider for the given binary id
     */
    void unregister(String binaryId);
    
    /**
     * returns a list of all binary ids which have an active BinaryTransformationProvider
     */
    List<String> showAll();

    /**
     * convert an object to OpenEngSBModelEntries with the help of the binary transformation provider registered under
     * the binary id
     */
    List<OpenEngSBModelEntry> convertToOpenEngSBModelEntries(String binaryId, Object object);

    /**
     * creates an object from OpenEngSBModelEntries with the help of the binary transformation provider registered under
     * the binary id
     */
    Object convertFromOpenEngSBModelEntries(String binaryId, List<OpenEngSBModelEntry> entries);
}
