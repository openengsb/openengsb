package org.openengsb.core.security.util;

import java.util.Map;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.security.internal.EntryUtils;
import org.openengsb.core.security.internal.model.EntryValue;
import org.openengsb.core.security.internal.model.PermissionData;

import com.google.common.collect.Maps;

public class PermissionUtils {
    
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
