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

package org.openengsb.core.services.internal.security.ldap;

import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;

/**
 * This class contains the attribute types for the Entries as String constants.
 * It also provides methods to build the various entries found in the
 * UserDataManager subtree of the DIT. The methods are specific to UserDataManager.
 * */
public final class SchemaConstants {

    public static final String ORGANIZATIONAL_UNIT_OC = "organizationalUnit";

    public static final String CN_ATTRIBUTE = "cn";
    public static final String JAVA_CLASS_NAME_ATTRIBUTE = "javaClassName";
    public static final String OBJECT_CLASS_ATTRIBUTE = "objectClass";
    public static final String OU_ATTRIBUTE = "ou";

    public static final String JAVA_CLASS_INSTANCE_OC = "org-openengsb-javaClassInstance";
    public static final String DESCRIPTIVE_OBJECT_OC = "org-openengsb-descriptiveObject";
    public static final String NAMED_OBJECT_OC = "org-openengsb-namedObject";
    
    public static final String STRING_ATTRIBUTE = "org-openengsb-string";
    public static final String EMPTY_FLAG_ATTRIBUTE = "org-openengsb-emptyFlag";

    private SchemaConstants() {
    }

    public static Dn openengsbBaseDn() {
        try {
            return new Dn("dc=openengsb,dc=org");
        } catch (LdapInvalidDnException e) {
            throw new LdapRuntimeException(e);
        }
    }

    public static Dn ouUserData() {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "userData", openengsbBaseDn());
    }

    public static Dn ouUsers() {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "users", ouUserData());
    }

    public static Dn ouGlobalPermissionSets() {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "permissionSets", ouUserData());
    }

    public static Dn user(String username) {
        return LdapUtils.concatDn(CN_ATTRIBUTE, username, ouUsers());
    }

    public static Dn ouUserAttributes(String username) {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "attributes", user(username));
    }

    public static Dn userAttribute(String username, String attributename) {
        return LdapUtils.concatDn(CN_ATTRIBUTE, attributename, ouUserAttributes(username));
    }

    public static Dn ouUserCredentials(String username) {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "credentials", user(username));
    }

    public static Dn userCredentials(String username, String credentials) {
        return LdapUtils.concatDn(CN_ATTRIBUTE, credentials, ouUserCredentials(username));
    }

    public static Dn ouUserPermissions(String username) {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "permissions", user(username));
    }

    public static Dn ouUserPermissionsDirect(String username) {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "direct", ouUserPermissions(username));
    }

    public static Dn ouUserPermissionSets(String username) {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "permissionSets", ouUserPermissions(username));
    }

    public static Dn userPermissionSet(String username, String permissionSet) {
        return LdapUtils.concatDn(CN_ATTRIBUTE, permissionSet, ouUserPermissionSets(username));
    }

    public static Dn globalPermissionSet(String name) {
        return LdapUtils.concatDn(CN_ATTRIBUTE, name, ouGlobalPermissionSets());
    }

    public static Dn ouGlobalPermissionsDirect(String permissionSet) {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "direct", globalPermissionSet(permissionSet));
    }

    public static Dn ouGlobalPermissionSetChildren(String permissionSet) {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "childrenSets", globalPermissionSet(permissionSet));
    }

    public static Dn globalPermissionChild(String parent, String child) {
        return LdapUtils.concatDn(CN_ATTRIBUTE, child, ouGlobalPermissionSetChildren(parent));
    }

    public static Dn ouGlobalPermissionSetAttributes(String permissionSet) {
        return LdapUtils.concatDn(OU_ATTRIBUTE, "attributes", globalPermissionSet(permissionSet));
    }

    public static Dn globalPermissionSetAttribute(String permissionSet, String attributename) {
        return LdapUtils.concatDn(CN_ATTRIBUTE, attributename, ouGlobalPermissionSetAttributes(permissionSet));
    }

}
