package org.openengsb.infrastructure.ldap.internal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.shared.ldap.model.cursor.EntryCursor;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.DefaultEntry;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.message.AddRequest;
import org.apache.directory.shared.ldap.model.message.AddRequestImpl;
import org.apache.directory.shared.ldap.model.message.DeleteRequest;
import org.apache.directory.shared.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.shared.ldap.model.message.LdapResult;
import org.apache.directory.shared.ldap.model.message.ModifyRequest;
import org.apache.directory.shared.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.model.message.ResultResponse;
import org.apache.directory.shared.ldap.model.message.SearchScope;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.schema.AttributeType;
import org.apache.directory.shared.ldap.model.schema.SchemaManager;
import org.apache.directory.shared.ldap.model.schema.registries.DefaultSchema;
import org.apache.directory.shared.ldap.model.schema.registries.Schema;
import org.apache.directory.shared.ldap.model.schema.registries.SchemaLoader;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.NoSuchAttributeException;
import org.openengsb.core.api.security.service.NoSuchCredentialsException;
import org.openengsb.core.api.security.service.PermissionSetAlreadyExistsException;
import org.openengsb.core.api.security.service.PermissionSetNotFoundException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.core.security.internal.EntryUtils;
import org.openengsb.core.security.internal.model.EntryElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDataManagerLdap implements UserDataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataManagerLdap.class);

    /* use a schema manager with DNs! (why?)
     * dc=openengsb,dc=org
     * ou=users
     */

    /* IMPORTANT!! DO NOT DELETE!!
     * IN ORDER TO STORE PASSWORDS IN PLAINTEXT, SET
     * DN: ads-interceptorId=passwordHashingInterceptor,ou=interceptors,ads-directoryServiceId=default,ou=config
     * ads-enabled to FALSE (in apacheds). requires restart of server to take effect.
     * 
     * NOTE: correct copy-paste error in description of UserDataManager.setUserAttribute:
     * 2nd exception is not thrown
     * 
     * check order when storing and retrieving lists of things!
     * 
     * */

    private LdapConnection connection;
    //private SchemaManager schemaManager;

    private String dnUser = "uid=%s,ou=users,dc=openengsb,dc=org";
    private String dnCredentials = "cn=%s,ou=credentials,uid=%s,ou=users,dc=openengsb,dc=org";
    private String dnAttribute = "cn=%s,ou=attributes,uid=%s,ou=users,dc=openengsb,dc=org";
    private String objectclass = "objectclass";

    public UserDataManagerLdap(){
    }

    public void setConnection(LdapConnection connection) {
        this.connection = connection;
    }

    public void loadSchemaManager(){
        
//        String openengsb = "openengsb";
//        Schema openengsbSchema = new DefaultSchema(openengsb);
//        
//        try {
//            
//            connection.loadSchema();
//            schemaManager = connection.getSchemaManager();
//            
//            
//            
//            AttributeType javaClassNameAttributeType = schemaManager.getAttributeType("javaClassName");
//            
//            System.out.println(javaClassNameAttributeType != null);
//            System.out.println(javaClassNameAttributeType);
//            
//            AttributeType psav = schemaManager.getAttributeType("openengsb-permissionSetAttributeValue");
//            
//            System.out.println(psav != null);
//            System.out.println(psav);
//            
//            List<Schema> enabledSchemas = schemaManager.getEnabled();
//            
//            System.out.println("enabled");
//            for(Schema s : enabledSchemas){
//                System.out.println(s);
//            }
//            
//            List<Schema> disabledSchemas = schemaManager.getDisabled();
//            
//            System.out.println("disabled");
//            for(Schema s : disabledSchemas){
//                System.out.println(s);
//            }
//            
//            
//            
//            
////            SchemaLoader loader = schemaManager.getLoader();
////            loader.addSchema(openengsbSchema);
////            
////            schemaManager.enable(openengsb);
////            schemaManager.load(openengsb);
////            
////            schemaManager.loadAllEnabled();
////            
////            loader.loadAttributeTypes(openengsbSchema);
////            loader.loadObjectClasses(openengsbSchema);
//            
////            Schema core = schemaManager.getLoadedSchema("core");
////            LOGGER.warn(new Boolean(core.getContent().isEmpty()).toString());
////            
////            LOGGER.warn("loaded: " + new Boolean(schemaManager.isSchemaLoaded(openengsb)).toString());
////            LOGGER.warn("enabled: " + new Boolean(schemaManager.isEnabled(openengsb)).toString());
////            
////            AttributeTypeRegistry registry = schemaManager.getAttributeTypeRegistry();
////            registry.renameSchema(openengsb, "haha");
////            
////            LOGGER.warn("javaValue: " + new Boolean(registry.contains("javaValue")).toString());
////            LOGGER.warn("javaClassName: " + new Boolean(registry.contains("javaClassName")).toString());
////            LOGGER.warn("pd: " + new Boolean(registry.contains("pd")).toString());
////            
////            ObjectClassRegistry or = schemaManager.getObjectClassRegistry();
////            
////            
////            LOGGER.warn("namedEntity: " + new Boolean(or.contains("namedEntity")).toString());
////            LOGGER.warn("organizationalUnit: " + new Boolean(or.contains("organizationalUnit")).toString());
////            LOGGER.warn("openengsbUser: " + new Boolean(registry.contains("openengsbUser")).toString());
////            LOGGER.warn("userAttribute: " + new Boolean(registry.contains("userAttribute")).toString());
////            
////            for(Schema schema: schemaManager.getEnabled()){
////                LOGGER.warn(schema.getSchemaName());
////            }
//            
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            throw new RuntimeException(e);
//        }
    }

    
    private void storeSorted(Entry entry){
        
    }
    
    
    @Override
    public Collection<String> getUserList() {
        String baseDn = "ou=users,dc=openengsb,dc=org";
        String filter = "(objectclass=openengsbUser)";
        String attribute = "uid";
        String username;
        Dn dn;
        EntryCursor entryCursor;
        Collection<String> userList = new LinkedList<String>();

        try {
//            dn = new Dn(schemaManager, baseDn);
            dn = new Dn(baseDn);
            entryCursor = connection.search(dn, filter, SearchScope.ONELEVEL, attribute); 
            while (entryCursor.next()) {
                username = entryCursor.get().get(attribute).getString();
                userList.add(username);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return userList;
    }

    private AddRequest buildAddRequestUser(String username) throws LdapException{

        String dn = String.format(dnUser, username);

        Object[] attributes = new String[] {
                objectclass, "top",
                objectclass, "openengsbUser",
                "uid", username};
        
//        Entry entry = new DefaultEntry(schemaManager, dn, attributes);
        Entry entry = new DefaultEntry(dn, attributes);
        return new AddRequestImpl().setEntry(entry);
    }

    @Override
    public void createUser(String username) throws UserExistsException {

        ResultResponse response;
        LdapResult result;

        try {
            response = connection.add(buildAddRequestUser(username));
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        result = response.getLdapResult();

        if(result.getResultCode() == ResultCodeEnum.ENTRY_ALREADY_EXISTS){
            throw new UserExistsException();
        }else if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            throw new RuntimeException(result.getDiagnosticMessage());
        } else {
            String[] children = new String[] {"credentials", "attributes", "permissions"};
            for(String child: children){
                try {
                    connection.add(buildAddRequestUserChild(child, username));
                } catch (LdapException e) {
                    LOGGER.warn("inconsistent state possible.");
                    throw new RuntimeException(e);
                }    
            }
        }
    }

    private AddRequest buildAddRequestUserChild(String ou, String username) throws LdapException{

        String dn = String.format("ou=%s,%s", ou, String.format(dnUser, username));

        Object[] attributes = new String[] {
                objectclass, "top",
                objectclass, "organizationalUnit",
                "ou", ou};

        Entry entry = new DefaultEntry(dn, attributes);
//        Entry entry = new DefaultEntry(schemaManager, dn, attributes);
        return new AddRequestImpl().setEntry(entry);
    }

    @Override
    public void deleteUser(String username) {

        LdapResult result;

        try {
            Dn userDn = new Dn(String.format(dnUser, username));
            result = deleteTree(userDn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(result.getResultCode() == ResultCodeEnum.NO_SUCH_OBJECT){
            LOGGER.warn("No such user, " + username);
        }else if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            throw new RuntimeException(result.getDiagnosticMessage());
        }
    }

    private AddRequest buildAddRequestCredentials(String username, String type, String value) throws LdapException{

        String dn = String.format(dnCredentials, type, username);

        Object[] attributes = new String[] {
                objectclass, "top",
                objectclass, "userCredentials",
                objectclass, "namedEntity",
                "cn", type,
                "userPassword", value};

        Entry entry = new DefaultEntry(dn, attributes);
//        Entry entry = new DefaultEntry(schemaManager, dn, attributes);
        return new AddRequestImpl().setEntry(entry);
    }

    private ModifyRequest buildModifyRequestCredentials(String username, String type, String value) throws LdapException{
        Dn dn = new Dn(String.format(dnCredentials, type, username));
//        Dn dn = new Dn(schemaManager, String.format(dnCredentials, type, username));
        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setName(dn);
        return modifyRequest.replace("userPassword", value);
    }

    private DeleteRequest buildDeleteRequestCredentials(String username, String type) throws LdapException{
        Dn dn = new Dn(String.format(dnCredentials, type, username));
//        Dn dn = new Dn(schemaManager, String.format(dnCredentials, type, username));
        return new DeleteRequestImpl().setName(dn);
    }

    @Override
    public void setUserCredentials(String username, String type, String value) throws UserNotFoundException {

        ResultResponse response;
        LdapResult result;

        try {
            if(!connection.exists(String.format(dnUser, username))){
                throw new UserNotFoundException();
            } else if (!connection.exists(String.format(dnCredentials, type, username))){
                response = connection.add(buildAddRequestCredentials(username, type, value));
            } else {
                response = connection.modify(buildModifyRequestCredentials(username, type, value));
            }
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        result = response.getLdapResult();

        if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            throw new RuntimeException(result.getDiagnosticMessage());
        }
    }

    @Override
    public String getUserCredentials(String username, String key) throws UserNotFoundException, NoSuchCredentialsException {
        String credentialsDn = String.format(this.dnCredentials, key, username);
        String userPassword = "userPassword";
        try {
            if(!connection.exists(String.format(dnUser, username))){
                throw new UserNotFoundException();
            }else if(!connection.exists(credentialsDn)){
                throw new NoSuchCredentialsException();
            }
            return connection.lookup(credentialsDn, userPassword).get(userPassword).get().getString();
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeUserCredentials(String username, String type) throws UserNotFoundException {

        ResultResponse response;
        LdapResult result;

        try {
            if(!connection.exists(String.format(dnUser, username))){
                throw new UserNotFoundException();
            }
            response = connection.delete(buildDeleteRequestCredentials(username, type));
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }

        result = response.getLdapResult();

        if(result.getResultCode() == ResultCodeEnum.NO_SUCH_OBJECT){
            LOGGER.warn("No such credentials, " +  type);
        } else if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            throw new RuntimeException(result.getDiagnosticMessage());
        }
    }

    @Override
    public void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException {

        String attNameDn = String.format(this.dnAttribute, attributename, username);

        try {
            if(!connection.exists(String.format(dnUser, username))){
                throw new UserNotFoundException();
            } else {
                AddRequest addRequest = buildAddRequestAttributeName(username, attributename);
                LOGGER.warn("adding attribute NAME: " + addRequest.getEntryDn().getName());
                ResultResponse response = connection.add(addRequest);
                LOGGER.warn(response.getLdapResult().getResultCode().toString());
                Dn dn = new Dn(attNameDn);
//                Dn dn = new Dn(schemaManager, attNameDn);
                deleteChildren(dn);
            }

            for(int position = 0; position < value.length; position++){
                
                AddRequest addRequest = buildAddRequestAttributeValue(position, attNameDn, value[position]);
                LOGGER.warn("adding attribute VALUE: " + addRequest.getEntryDn().getName());
                ResultResponse response = connection.add(addRequest);
                if(response.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS){
                    throw new Exception(response.getLdapResult().getDiagnosticMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private AddRequest buildAddRequestAttributeName(String username, String attributename) throws LdapException{

        String dn = String.format(dnAttribute, attributename, username);

        Object[] attributes = new String[] {
                objectclass, "top",
                objectclass, "namedEntity",
                "cn", attributename};

        return new AddRequestImpl().setEntry(new DefaultEntry(dn, attributes));
//        return new AddRequestImpl().setEntry(new DefaultEntry(schemaManager, dn, attributes));
    }

    private AddRequest buildAddRequestAttributeValue(int position, String baseDn, Object value) throws LdapException{

        LOGGER.warn("baseDn: " + baseDn);
        LOGGER.warn(new Boolean(connection.exists(baseDn)).toString());
        
        EntryElement entryElement = EntryUtils.makeEntryElement(value);
        String attValueDn = String.format("cn=%s,%s", position, baseDn);

        LOGGER.warn("total dn: " + attValueDn);
        LOGGER.warn(entryElement.toString());
        LOGGER.warn("only the value: " + entryElement.getValue());
        
        Object[] attributes = new String[] {
                objectclass, "top",
                objectclass, "javaObject",
                objectclass, "userAttribute",
                objectclass, "namedEntity",
                "cn", String.valueOf(position),
                "javaClassName", entryElement.getType(),
                "javaValue", entryElement.getValue()
                };


        
        Entry entry = new DefaultEntry(attValueDn, attributes);
//        Entry entry = new DefaultEntry(schemaManager, attValueDn, attributes);
        return new AddRequestImpl().setEntry(entry);
    }

    private LdapResult deleteTree(Dn root) throws Exception{
        
        EntryCursor entryCursor = connection.search(root, "(objectclass=*)", SearchScope.ONELEVEL);

        while(entryCursor.next()){
            deleteTree(entryCursor.get().getDn());
        }
        
        DeleteRequest deleteRequest = new DeleteRequestImpl();
        deleteRequest.setName(root);
        LOGGER.warn("deleting: " + root.getName());
        return connection.delete(deleteRequest).getLdapResult();
    }

    private void deleteChildren(Dn root) throws Exception{
        EntryCursor entryCursor = connection.search(root, "(objectclass=*)", SearchScope.ONELEVEL);
        while (entryCursor.next()) {
            deleteTree(entryCursor.get().getDn());
        }
    }

    @Override
    public void removeUserAttribute(String username, String attributename) throws UserNotFoundException {

        String attDn = String.format(dnAttribute, attributename, username);
        LdapResult result;

        try {
            if(!connection.exists(String.format(dnUser, username))){
                throw new UserNotFoundException();
            } else if(!connection.exists(attDn)){
                LOGGER.warn("No such attribute, " +  attributename);
                return;
            } else {
                Dn dn = new Dn(attDn);
//                Dn dn = new Dn(schemaManager, attDn);
                result = deleteTree(dn);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(result.getResultCode() == ResultCodeEnum.NO_SUCH_OBJECT){
            LOGGER.warn("No such user, " + username);
        }else if(result.getResultCode() != ResultCodeEnum.SUCCESS){
            throw new RuntimeException(result.getDiagnosticMessage());
        }
    }

    private List<Object> getAttribute(String dn) throws Exception{

        SortedMap<Integer, EntryElement> entryElements = new TreeMap<Integer, EntryElement>();
        String filter = "(objectclass=namedEntity)";
        String cn = "cn";
        String javaClassName = "javaClassName";
        String javaValue = "javaValue";
        EntryCursor entryCursor = connection.search(dn, filter, SearchScope.ONELEVEL, cn, javaClassName, javaValue);

        while(entryCursor.next()){
            Entry entry = entryCursor.get();
            Integer position = Integer.valueOf(entry.get(cn).get().getString());
            String type = entry.get(javaClassName).get().getString();
            String value = entry.get(javaValue).get().getString();
            EntryElement entryElement = new EntryElement(type, value);
            entryElements.put(position, entryElement);
        }

        List<EntryElement> entryElementList = new LinkedList<EntryElement>(entryElements.values());
        return EntryUtils.convertAllEntryElementsToObject(entryElementList);
    }

    @Override
    public List<Object> getUserAttribute(String username, String attributename) throws UserNotFoundException, NoSuchAttributeException {

        String attDn = String.format(dnAttribute, attributename, username);

        try {
            if(!connection.exists(String.format(dnUser, username))){
                throw new UserNotFoundException();
            }else if(!connection.exists(attDn)){
                throw new NoSuchAttributeException();
            }

            return getAttribute(attDn);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addPermissionToUser(String username, Permission... permission) throws UserNotFoundException {
        /*
         * adds an ordered list of permissions to the user -> getPermissionsForUser() should return the
         * permission in the same order as they were presented here
         * Unlike permissionSets, there is no point where a permission needs to be registered. Therefore
         * no references need to be checked. 
         * */
        throw new UnsupportedOperationException();

    }

    @Override
    public void addPermissionSetToPermissionSet(String permissionSetParent, String... permissionSet) throws PermissionSetNotFoundException {
        /*
         * Adds a node in the permissionSet tree. 
         * */
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
    public void createPermissionSet(String permissionSet, Permission... permission) throws PermissionSetAlreadyExistsException {
        /*
         * Creates a permissionSet granting the given Permissions. The permissions should be retrieved in the order they
         * were presented. Requires List.
         * */
        throw new UnsupportedOperationException();

    }

    @Override
    public Collection<Permission> getAllPermissionsForUser(String arg0) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    /*
     * B extends A. Permission with runtimetype B is inserted.
     * Search filter with type A is applied. Return the permission?
     * org.openengsb.core.common.util.CollectionUtilsExtended will
     * take care of it.
     * */
    @Override
    public <T extends Permission> Collection<T> getAllPermissionsForUser(String username, Class<T> type) throws UserNotFoundException {
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
        /*
         * Returns a collection of permissions. Set extends Collection. Still it should be a list
         * which contains the permissions in the same order as they were added to the user.
         * */
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Permission> Collection<T> getPermissionsForUser(String username, Class<T> type) throws UserNotFoundException {
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

}
