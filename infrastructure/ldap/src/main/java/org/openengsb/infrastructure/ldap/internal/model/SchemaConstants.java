package org.openengsb.infrastructure.ldap.internal.model;

import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;

public class SchemaConstants {

    /*can we make these final?*/

    public static String uidObjectOc = "uidObject";
    public static String organizationalUnitOc = "organizationalUnit";
    public static String javaClassInstanceOc = "openengsb-javaClassInstance";
    public static String descriptiveObjectOc = "openengsb-descriptiveObject";
    public static String namedObjectOc = "openengsb-namedObject";

    public static String uniqueObjectOc = "openengsb-uniqueObject";
    public static String awareContainerOc = "openengsb-awareContainer";

    public static String uidAttribute = "uid";
    public static String cnAttribute = "cn";
    public static String javaClassNameAttribute = "javaClassName";
    public static String objectClassAttribute = "objectClass";
    public static String ouAttribute = "ou";
    public static String descriptionAttribute = "description";
    public static String emptyFlagAttribute = "openengsb-emptyFlag";

    public static String idAttribute = "openengsb-id";
    public static String maxIdAttribute = "openengsb-maxId";

    public static Dn openengsbBaseDn() {
        try {
            return new Dn("dc=openengsb,dc=org");
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn userDataBaseDn() {
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "userData");
            return new Dn(rdn, openengsbBaseDn());
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouUsers() {
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "users");
            return new Dn(rdn, userDataBaseDn());
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouGlobalPermissionSets() {
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "permissionSets");
            return new Dn(rdn, userDataBaseDn());
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn user(String username) {
        try {
            Rdn rdn = new Rdn(SchemaConstants.cnAttribute, username);
            return new Dn(rdn, ouUsers());
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouUserAttributes(String username) {
        try {
            Rdn userRdn = new Rdn(SchemaConstants.cnAttribute, username);
            Dn dn = new Dn(userRdn, ouUsers());
            return dn.add(new Rdn(SchemaConstants.ouAttribute, "attributes"));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn userAttribute(String username, String attributename) {
        try {
            Rdn userRdn = new Rdn(SchemaConstants.cnAttribute, attributename);
            return new Dn(userRdn, ouUserAttributes(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouUserCredentials(String username){
        try {
            Rdn userRdn = new Rdn(SchemaConstants.ouAttribute, "credentials");
            return new Dn(userRdn, user(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn userCredentials(String username, String credentials){
        try {
            Rdn userRdn = new Rdn(SchemaConstants.cnAttribute, credentials);
            return new Dn(userRdn, ouUserCredentials(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouUserPermissions(String username){
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "permissions");
            return new Dn(rdn, user(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn ouUserPermissionsDirect(String username){
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "direct");
            return new Dn(rdn, ouUserPermissions(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Dn ouUserPermissionSets(String username){
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "permissionSets");
            return new Dn(rdn, ouUserPermissions(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Dn userPermissionSet(String username, String permissionSet){
        try {
            Rdn rdn = new Rdn(SchemaConstants.cnAttribute, permissionSet);
            return new Dn(rdn, ouUserPermissionSets(username));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn globalPermissionSet(String name){
        try {
            Rdn rdn = new Rdn(SchemaConstants.cnAttribute, name);
            return new Dn(rdn, ouGlobalPermissionSets());
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Dn ouGlobalPermissionsDirect(String permissionSet){
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "direct");
            return new Dn(rdn, globalPermissionSet(permissionSet));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Dn ouGlobalPermissionSetChildren(String permissionSet){
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "childrenSets");
            return new Dn(rdn, globalPermissionSet(permissionSet));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static Dn globalPermissionChild(String parent, String child){
        try {
            Rdn rdn = new Rdn(SchemaConstants.cnAttribute, child);
            return new Dn(rdn, ouGlobalPermissionSetChildren(parent));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Dn ouGlobalPermissionSetAttributes(String permissionSet){
        try {
            Rdn rdn = new Rdn(SchemaConstants.ouAttribute, "attributes");
            return new Dn(rdn, globalPermissionSet(permissionSet));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Dn globalPermissionSetAttribute(String permissionSet, String attributename){
        try {
            Rdn rdn = new Rdn(SchemaConstants.cnAttribute, attributename);
            return new Dn(rdn, ouGlobalPermissionSetAttributes(permissionSet));
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }
    
}

//    public static Dn globalPermission(String permissionSet, String permission){
//        try {
//            Rdn rdn = new Rdn(SchemaConstants.cnAttribute, "name");
//            return new Dn(rdn, ouGlobalPermissionSet(permissionSet));
//        } catch (LdapInvalidDnException e) {
//            throw new RuntimeException(e);
//        }
//    }


