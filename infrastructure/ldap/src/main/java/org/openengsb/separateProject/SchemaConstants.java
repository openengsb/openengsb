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

package org.openengsb.separateProject;

import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.infrastructure.ldap.internal.LdapGeneralException;
import org.openengsb.infrastructure.ldap.util.LdapUtils;

/**
 * This class contains the attribute types for the Entries as String constants.
 * It also provides methods to build the various entries found in the
 * UserDataManager subtree of the DIT. The methods are specific to UserDataManager.
 * */
public final class SchemaConstants {

    public static final String organizationalUnitOc = "organizationalUnit";

    public static final String cnAttribute = "cn";
    public static final String javaClassNameAttribute = "javaClassName";
    public static final String objectClassAttribute = "objectClass";
    public static final String ouAttribute = "ou";

    public static final String javaClassInstanceOc = "org-openengsb-javaClassInstance";
    public static final String descriptiveObjectOc = "org-openengsb-descriptiveObject";
    public static final String namedObjectOc = "org-openengsb-namedObject";
    
    public static final String stringAttribute = "org-openengsb-string";
    public static final String emptyFlagAttribute = "org-openengsb-emptyFlag";

    private SchemaConstants() {
    }

    public static final Dn openengsbBaseDn() {
        try {
            return new Dn("dc=openengsb,dc=org");
        } catch (LdapInvalidDnException e) {
            throw new LdapGeneralException(e);
        }
    }

    public static final Dn ouUserData() {
        return LdapUtils.concatDn(ouAttribute, "userData", openengsbBaseDn());
    }

    public static final Dn ouUsers() {
        return LdapUtils.concatDn(ouAttribute, "users", ouUserData());
    }

    public static final Dn ouGlobalPermissionSets() {
        return LdapUtils.concatDn(ouAttribute, "permissionSets", ouUserData());
    }

    public static final Dn user(String username) {
        return LdapUtils.concatDn(cnAttribute, username, ouUsers());
    }

    public static final Dn ouUserAttributes(String username) {
        return LdapUtils.concatDn(ouAttribute, "attributes", user(username));
    }

    public static final Dn userAttribute(String username, String attributename) {
        return LdapUtils.concatDn(cnAttribute, attributename, ouUserAttributes(username));
    }

    public static final Dn ouUserCredentials(String username) {
        return LdapUtils.concatDn(ouAttribute, "credentials", user(username));
    }

    public static final Dn userCredentials(String username, String credentials) {
        return LdapUtils.concatDn(cnAttribute, credentials, ouUserCredentials(username));
    }

    public static final Dn ouUserPermissions(String username) {
        return LdapUtils.concatDn(ouAttribute, "permissions", user(username));
    }

    public static final Dn ouUserPermissionsDirect(String username) {
        return LdapUtils.concatDn(ouAttribute, "direct", ouUserPermissions(username));
    }

    public static final Dn ouUserPermissionSets(String username) {
        return LdapUtils.concatDn(ouAttribute, "permissionSets", ouUserPermissions(username));
    }

    public static final Dn userPermissionSet(String username, String permissionSet) {
        return LdapUtils.concatDn(cnAttribute, permissionSet, ouUserPermissionSets(username));
    }

    public static final Dn globalPermissionSet(String name) {
        return LdapUtils.concatDn(cnAttribute, name, ouGlobalPermissionSets());
    }

    public static final Dn ouGlobalPermissionsDirect(String permissionSet) {
        return LdapUtils.concatDn(ouAttribute, "direct", globalPermissionSet(permissionSet));
    }

    public static final Dn ouGlobalPermissionSetChildren(String permissionSet) {
        return LdapUtils.concatDn(ouAttribute, "childrenSets", globalPermissionSet(permissionSet));
    }

    public static final Dn globalPermissionChild(String parent, String child) {
        return LdapUtils.concatDn(cnAttribute, child, ouGlobalPermissionSetChildren(parent));
    }

    public static final Dn ouGlobalPermissionSetAttributes(String permissionSet) {
        return LdapUtils.concatDn(ouAttribute, "attributes", globalPermissionSet(permissionSet));
    }

    public static final Dn globalPermissionSetAttribute(String permissionSet, String attributename) {
        return LdapUtils.concatDn(cnAttribute, attributename, ouGlobalPermissionSetAttributes(permissionSet));
    }

}
