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

import java.util.Date;

import org.openengsb.core.edb.api.EDBObjectEntry;
import org.openengsb.core.edb.jpa.internal.JPAEntry;

/**
 * The DateConverterStep is the step which shall be used if the entry type is a Date.
 */
public class DateConverterStep implements EDBConverterStep {

    @Override
    public Boolean doesStepFit(String classname) {
        return classname.equals(Date.class.getName());
    }

    @Override
    public JPAEntry convertToJPAEntry(EDBObjectEntry entry) {
        return new JPAEntry(entry.getKey(), "" + ((Date) entry.getValue()).getTime(), entry.getType());
    }

    @Override
    public EDBObjectEntry convertToEDBObjectEntry(JPAEntry entry) {
        return new EDBObjectEntry(entry.getKey(), new Date(Long.parseLong(entry.getValue())), entry.getType());
    }

}
