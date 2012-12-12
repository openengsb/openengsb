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

package org.openengsb.core.services.internal.security;

import java.util.Map;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.services.internal.security.model.EntryValue;
import org.openengsb.core.services.internal.security.model.PermissionData;

import com.google.common.collect.Maps;




/**
 * Provides util-functions for handling Permissions.
 * */
public final class PermissionUtils {

    private PermissionUtils() {
    }

    /** Converts a {@link Permission} into {@link PermissionData} to allow easier storing of the permission.
    */
         public static PermissionData convertPermissionToPermissionData(Permission permission) {
             PermissionData permissionData = new PermissionData();
             String type = permission.getClass().getName();
             permissionData.setType(type);
             Map<String, EntryValue> entryMap = EntryUtils.convertBeanToEntryMap(permission);
     
             // copy the map, because JPA does not like the transformed map for some reason
             entryMap = Maps.newHashMap(entryMap);
             permissionData.setAttributes(entryMap);
             return permissionData;

         }

    
  

}
