package org.openengsb.separateProject;

import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.infrastructure.ldap.util.LdapUtils;

public class SchemaConstants {

    /*can we make these final?*/

    public static String uidObjectOc = "uidObject";
    public static String organizationalUnitOc = "organizationalUnit";

    public static String javaClassInstanceOc = "openengsb-javaClassInstance";
    public static String descriptiveObjectOc = "openengsb-descriptiveObject";
    public static String namedObjectOc = "openengsb-namedObject";

    public static String cnAttribute = "cn";
    public static String javaClassNameAttribute = "javaClassName";
    public static String objectClassAttribute = "objectClass";
    public static String ouAttribute = "ou";

    public static String stringAttribute = "openengsb-string";
    public static String emptyFlagAttribute = "openengsb-emptyFlag";

    public static Dn openengsbBaseDn() {
        try {
            return new Dn("dc=openengsb,dc=org");
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouUserData() {
        return LdapUtils.concatDn(ouAttribute, "userData", openengsbBaseDn());
    }

    public static Dn ouUsers() {
        return LdapUtils.concatDn(ouAttribute, "users", ouUserData());
    }

    public static Dn ouGlobalPermissionSets() {
        return LdapUtils.concatDn(ouAttribute, "permissionSets", ouUserData());
    }

    public static Dn user(String username) {
        return LdapUtils.concatDn(cnAttribute, username, ouUsers());
    }

    public static Dn ouUserAttributes(String username) {
        return LdapUtils.concatDn(ouAttribute, "attributes", user(username));
    }

    public static Dn userAttribute(String username, String attributename) {
        return LdapUtils.concatDn(cnAttribute, attributename, ouUserAttributes(username));
    }

    public static Dn ouUserCredentials(String username){
        return LdapUtils.concatDn(ouAttribute, "credentials", user(username));
    }

    public static Dn userCredentials(String username, String credentials){
        return LdapUtils.concatDn(cnAttribute, credentials, ouUserCredentials(username));
    }

    public static Dn ouUserPermissions(String username){
        return LdapUtils.concatDn(ouAttribute, "permissions", user(username));
    }

    public static Dn ouUserPermissionsDirect(String username){
        return LdapUtils.concatDn(ouAttribute, "direct", ouUserPermissions(username));
    }

    public static Dn ouUserPermissionSets(String username){
        return LdapUtils.concatDn(ouAttribute, "permissionSets", ouUserPermissions(username));
    }

    public static Dn userPermissionSet(String username, String permissionSet){
        return LdapUtils.concatDn(cnAttribute, permissionSet, ouUserPermissionSets(username));
    }

    public static Dn globalPermissionSet(String name){
        return LdapUtils.concatDn(cnAttribute, name, ouGlobalPermissionSets());
    }

    public static Dn ouGlobalPermissionsDirect(String permissionSet){
        return LdapUtils.concatDn(ouAttribute, "direct", globalPermissionSet(permissionSet));
    }

    public static Dn ouGlobalPermissionSetChildren(String permissionSet){
        return LdapUtils.concatDn(ouAttribute, "childrenSets", globalPermissionSet(permissionSet));
    }

    public static Dn globalPermissionChild(String parent, String child){
        return LdapUtils.concatDn(cnAttribute, child, ouGlobalPermissionSetChildren(parent));
    }

    public static Dn ouGlobalPermissionSetAttributes(String permissionSet){
        return LdapUtils.concatDn(ouAttribute, "attributes", globalPermissionSet(permissionSet));
    }

    public static Dn globalPermissionSetAttribute(String permissionSet, String attributename){
        return LdapUtils.concatDn(cnAttribute, attributename, ouGlobalPermissionSetAttributes(permissionSet));
    }

}
