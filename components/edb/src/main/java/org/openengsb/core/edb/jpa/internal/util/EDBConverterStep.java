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

package org.openengsb.core.edb.jpa.internal.util;

import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.edb.jpa.internal.JPAEntry;

/**
 * An EDBConverterStep is a possible EDBObjectEntry <-> JPAEntry converting step. When the doesStepFit method returns
 * true the other two methods are used to perform a conversion, if there was no step earlier found which fits.
 */
public interface EDBConverterStep {

    /**
     * Returns true if the converter step should handle this type of class. False if not
     */
    Boolean doesStepFit(String classname);

    /**
     * Converts an EDBObjectEntry into a JPAEntry.
     */
    JPAEntry convertToJPAEntry(EDBObjectEntry entry);

    /**
     * Converts a JPAEntry into an EDBObjectEntry.
     */
    EDBObjectEntry convertToEDBObjectEntry(JPAEntry entry);
}
