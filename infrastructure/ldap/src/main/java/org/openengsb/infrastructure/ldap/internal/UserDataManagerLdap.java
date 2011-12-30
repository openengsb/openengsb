package org.openengsb.infrastructure.ldap.internal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.shared.ldap.model.cursor.EntryCursor;
import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.message.AddRequest;
import org.apache.directory.shared.ldap.model.message.AddRequestImpl;
import org.apache.directory.shared.ldap.model.message.AddResponse;
import org.apache.directory.shared.ldap.model.message.DeleteRequest;
import org.apache.directory.shared.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.shared.ldap.model.message.DeleteResponse;
import org.apache.directory.shared.ldap.model.message.LdapResult;
import org.apache.directory.shared.ldap.model.message.ModifyRequest;
import org.apache.directory.shared.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.model.message.ResultResponse;
import org.apache.directory.shared.ldap.model.message.SearchScope;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.NoSuchAttributeException;
import org.openengsb.core.api.security.service.NoSuchCredentialsException;
import org.openengsb.core.api.security.service.PermissionSetAlreadyExistsException;
import org.openengsb.core.api.security.service.PermissionSetNotFoundException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;

public class UserDataManagerLdap implements UserDataManager {

    //private static final Logger LOGGER = LoggerFactory.getLogger(UserDataManagerLdap.class);

    /* use a schema manager with DNs! (why?)
     * dc=openengsb,dc=org
     * ou=users
     */

    /* IMPORTANT!! DO NOT DELETE!!
     * IN ORDER TO STORE PASSWORDS IN PLAINTEXT, SET
     * DN: ads-interceptorId=passwordHashingInterceptor,ou=interceptors,ads-directoryServiceId=default,ou=config
     * ads-enabled to FALSE (in apacheds). requires restart of server to take effect.
     * 
     * */

    private LdapConnection connection;

    private String objectClass = "objectClass";
    private String objectClassTop = "top";
    private String organizationalUnit = "organizationalUnit";
    private String objectClassUser = "userObject";
    //the following 3 are both the ou of the subentry and its objectClass
    private String credentials = "credentials";
    private String attributes = "attributes";
    private String permissions = "permissions";

    private String userBaseDn;
    private String ou = "ou"; //this is the Rdn of the subentries
    private String userRdn = "uid"; //this is also the Rdn of a user entry
    private String credentialsRdn = "cn";
    private String credentialsType = "userPassword";
    //private String attributesRdn = "cn";

    public UserDataManagerLdap(){
    }

    public void setConnection(LdapConnection connection) {
        this.connection = connection;
    }

    public void setUserBaseDn(String userBaseDn) {
        this.userBaseDn = userBaseDn;
    }

    private String buildUserDn(String username){
        return String.format("%s=%s,%s",userRdn,username,userBaseDn);
    }

    private Entry buildUserEntry(String username) throws LdapException{
        return new DefaultEntry(
                buildUserDn(username),
                String.format("%s: %s", objectClass, objectClassTop),
                String.format("%s: %s", objectClass, objectClassUser),
                userRdn, username);
    }

    private String buildSubentryDn(String ou, String username){
        return String.format("%s=%s,%s",this.ou, ou, buildUserDn(username));
    }

    private Entry buildSubentry(String ou, String username) throws LdapException{
        return new DefaultEntry(
                buildSubentryDn(ou, username),
                String.format("%s: %s", objectClass, objectClassTop),
                String.format("%s: %s", objectClass, organizationalUnit),
                this.ou, ou);
    }

    private String buildCredentialsDn(String username, String type){
        return String.format("%s=%s,%s", this.credentialsRdn, type, buildSubentryDn(credentials, username));
    }

    private Entry buildCredentialsEntry(String username, String type, String value) throws LdapException{
        return new DefaultEntry(
                buildCredentialsDn(username, type),
                String.format("%s: %s", objectClass, objectClassTop),
                String.format("%s: %s", objectClass, credentials),
                this.credentialsRdn, type,
                this.credentialsType, value);
    }
    
    @Override
    public Collection<String> getUserList() {
        Collection<String> userList = new LinkedList<String>();
        try {
            EntryCursor entryCursor = connection.search(
                    userBaseDn, 
                    String.format("(%s=%s)", objectClass, objectClassUser),
                    SearchScope.ONELEVEL);
            while (entryCursor.next()) {
                userList.add(entryCursor.get().getDn().getRdn().getAva().getUpValue().getString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return userList;
    }

    @Override
    public void createUser(String username) throws UserExistsException {

        AddRequest addRequest = new AddRequestImpl();
        ResultResponse response = null;

        try {
            Entry entry = buildUserEntry(username);
            addRequest.setEntry(entry);
            response = connection.add(addRequest);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        LdapResult result = response.getLdapResult();

        if(result.getResultCode() == ResultCodeEnum.ENTRY_ALREADY_EXISTS){
            throw new UserExistsException();
        }else if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            throw new RuntimeException(result.getDiagnosticMessage());
        } else {
            try { 
                /* What about inconsistent state?
                 * there is 1 problem: if the connection is cut off between these 3 calls,
                 * then some subentries exist while the others don't. If the user is to be deleted,
                 * the delete method will fail because of NO_SUCH_OBJECT
                 * also any attempt to add an entry below the missing subentry will fail.
                 * Seems like there's no rollback in ldap..
                 * */
                connection.add(buildSubentry(credentials, username));
                connection.add(buildSubentry(attributes, username));
                connection.add(buildSubentry(permissions, username));
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void deleteSubtree(String username) throws Exception{
        String[] subentries = {credentials, attributes, permissions};
        for (String subentry : subentries) {
            String subentryDn = buildSubentryDn(subentry, username);
            EntryCursor entryCursor = connection.search(
                    subentryDn,
                    String.format("(%s=%s)", objectClass, subentry),
                    SearchScope.ONELEVEL);
            while (entryCursor.next()) {
                connection.delete(entryCursor.get().getDn());
            }
            connection.delete(subentryDn);
        }
        
        
    }

    @Override
    public void deleteUser(String username) {

        DeleteRequest deleteRequest = new DeleteRequestImpl();
        ResultResponse response = null;

        try {
            Dn userDn = new Dn(buildUserDn(username));
            if(connection.exists(userDn)){
                deleteSubtree(username);
                deleteRequest.setName(userDn);
                response = connection.delete(deleteRequest);
            }else{
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LdapResult result = response.getLdapResult();

        if(result.getResultCode() != ResultCodeEnum.SUCCESS 
                && result.getResultCode() != ResultCodeEnum.NO_SUCH_OBJECT){
            throw new RuntimeException(result.getDiagnosticMessage());
        }
    }

    @Override
    public void setUserCredentials(String username, String type, String value) throws UserNotFoundException {

        ResultResponse response = null;
        boolean credentialsExist = false;
        
        try {
            if(!connection.exists(buildUserDn(username))){
                throw new UserNotFoundException();
            }
            credentialsExist = connection.exists(buildCredentialsDn(username, type));
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        
        if(credentialsExist){
            
            Dn dn = null;
            try {
                dn = new Dn(buildCredentialsDn(username, type));
            } catch (LdapInvalidDnException e) {
                throw new RuntimeException(e);
            }
            
            ModifyRequest modifyRequest = new ModifyRequestImpl();
            modifyRequest.setName(dn);
            modifyRequest.replace(credentialsType, value);
            try {
                response = connection.modify(modifyRequest);
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }
        }else{
            AddRequest addRequest = new AddRequestImpl();
            try {
                Entry entry = buildCredentialsEntry(username, type, value);
                addRequest.setEntry(entry);
                response = connection.add(addRequest);
            } catch (LdapException e) {
                throw new RuntimeException(e);
            }
        }

        LdapResult result = response.getLdapResult();

        if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            throw new RuntimeException(result.getDiagnosticMessage());
        }
    }

    @Override
    public String getUserCredentials(String username, final String key) throws UserNotFoundException, NoSuchCredentialsException {
        String credentialsDn = buildCredentialsDn(username, key);
        try {
            if(!connection.exists(buildUserDn(username))){
                throw new UserNotFoundException();
            }else if(!connection.exists(credentialsDn)){
                throw new NoSuchCredentialsException();
            }
            return connection.lookup(credentialsDn).get(credentialsType).get().getString();
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeUserCredentials(String username, String type) throws UserNotFoundException {
        
        try {
            if(!connection.exists(buildUserDn(username))){
                throw new UserNotFoundException();
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
        
        DeleteRequest deleteRequest = new DeleteRequestImpl();
        ResultResponse response = null;

        try {
            Dn dn = new Dn(buildCredentialsDn(username, type));
            deleteRequest.setName(dn);
            response = connection.delete(deleteRequest);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        LdapResult result = response.getLdapResult();

        if(result.getResultCode() != ResultCodeEnum.SUCCESS 
                && result.getResultCode() != ResultCodeEnum.NO_SUCH_OBJECT){
            throw new RuntimeException(result.getDiagnosticMessage());
        }
    }
    
    /* create new attributeType 'userAttribute'
     * What is m-multivalue vs m-singlevalue
     * What is m-collective
     * check das und dann sollts klar sein wie mans macht.
     * */

    @Override
    public void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException {
        throw new UnsupportedOperationException();

    }
    
    @Override
    public void removeUserAttribute(String arg0, String arg1) throws UserNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public List<Object> getUserAttribute(String arg0, String arg1) throws UserNotFoundException, NoSuchAttributeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPermissionSetToPermissionSet(String arg0, String... arg1) throws PermissionSetNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addPermissionSetToUser(String arg0, String... arg1) throws UserNotFoundException, PermissionSetNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addPermissionToSet(String arg0, Permission... arg1) throws PermissionSetNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addPermissionToUser(String arg0, Permission... arg1) throws UserNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void createPermissionSet(String arg0, Permission... arg1) throws PermissionSetAlreadyExistsException {
        throw new UnsupportedOperationException();

    }

    @Override
    public Collection<Permission> getAllPermissionsForUser(String arg0) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Permission> Collection<T> getAllPermissionsForUser(String arg0, Class<T> arg1) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Permission> getAllPermissionsFromPermissionSet(String arg0) throws PermissionSetNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPermissionSetAttribute(String arg0, String arg1) throws PermissionSetNotFoundException, NoSuchAttributeException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getPermissionSetList() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getPermissionSetsFromPermissionSet(String arg0) throws PermissionSetNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getPermissionSetsFromUser(String arg0) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Permission> getPermissionsForUser(String arg0) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Permission> Collection<T> getPermissionsForUser(String arg0, Class<T> arg1) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Permission> getPermissionsFromPermissionSet(String arg0) throws PermissionSetNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePermissionFromSet(String arg0, Permission... arg1) throws PermissionSetNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removePermissionFromUser(String arg0, Permission... arg1) throws UserNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removePermissionSetFromPermissionSet(String arg0, String... arg1) throws PermissionSetNotFoundException {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removePermissionSetFromUser(String arg0, String... arg1) throws UserNotFoundException {
        throw new UnsupportedOperationException();

    }
    
    @Override
    public void setPermissionSetAttribute(String arg0, String arg1, String arg2) throws PermissionSetNotFoundException {
        throw new UnsupportedOperationException();

    }

    //  private String objectClassCredentials = "credentials";
    //  private String objectClassAttributes = "attributes";
    //  private String objectClassPermissions = "permissions";
    //  private String ouCredentials;
    //  private String ouAttributes;
    //  private String ouPermissions;
    //  private String credentialsUserPassword;
    //  private String attributesUniqueMember;

    //  public void setObjectClassUser(String objectClassUser) {
    //  this.objectClassUser = objectClassUser;
    //}
    //
    //public void setObjectClassTop(String objectClassTop) {
    //  this.objectClassTop = objectClassTop;
    //}
    //
    //public void setObjectClassOrganizationalUnit(String objectClassOrganizationalUnit) {
    //  this.objectClassOrganizationalUnit = objectClassOrganizationalUnit;
    //}
    //

    //
    //public void setUid(String uid) {
    //  this.uid = uid;
    //}
    //
    //public void setCredentials(String credentials) {
    //  this.credentials = credentials;
    //}
    //
    //public void setAttributes(String attributes) {
    //  this.attributes = attributes;
    //}
    //
    //public void setPermissions(String permissions) {
    //  this.permissions = permissions;
    //}

    //public void setObjectClassCredentials(String objectClassCredentials) {
    //  this.objectClassCredentials = objectClassCredentials;
    //}
    //
    //public void setObjectClassAttributes(String objectClassAttributes) {
    //  this.objectClassAttributes = objectClassAttributes;
    //}
    //
    //public void setObjectClassPermissions(String objectClassPermissions) {
    //  this.objectClassPermissions = objectClassPermissions;
    //}
    //
    //public void setCn(String cn) {
    //  this.cn = cn;
    //}
    //
    //public void setCredentialsUserPassword(String credentialsUserPassword) {
    //  this.credentialsUserPassword = credentialsUserPassword;
    //}
    //
    //public void setAttributesUniqueMember(String attributesUniqueMember) {
    //  this.attributesUniqueMember = attributesUniqueMember;
    //}

}
