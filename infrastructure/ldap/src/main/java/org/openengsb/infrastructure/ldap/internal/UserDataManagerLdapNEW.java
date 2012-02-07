package org.openengsb.infrastructure.ldap.internal;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.name.Dn;
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
import org.openengsb.infrastructure.ldap.internal.dao.LdapDao;
import org.openengsb.infrastructure.ldap.internal.model.EntryFactory;
import org.openengsb.infrastructure.ldap.internal.model.OrderFilter;
import org.openengsb.infrastructure.ldap.internal.model.SchemaConstants;
import org.openengsb.infrastructure.ldap.internal.model.TreeStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDataManagerLdapNEW implements UserDataManager {

    private LdapDao dao;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataManagerLdapNEW.class);

    public void setLdapDao(LdapDao dao){
        this.dao = dao;
    }

    public LdapDao getDao(){
        return dao;
    }
    
    /* IMPORTANT!! DO NOT DELETE!!
     * IN ORDER TO STORE PASSWORDS IN PLAINTEXT, SET
     * DN: ads-interceptorId=passwordHashingInterceptor,ou=interceptors,ads-directoryServiceId=default,ou=config
     * ads-enabled to FALSE (in apacheds). requires restart of server to take effect.
     * 
     * NOTE: correct copy-paste error in description of UserDataManager.setUserAttribute:
     * 2nd exception is not thrown
     * */

    private List<EntryElement> makeEntryElementListFromPermissions(Permission... permissions){
        List<EntryElement> entryElements = new LinkedList<EntryElement>();
        for(Permission p : permissions){
            entryElements.add(new EntryElement(p.getClass().getName(), p.describe()));
        }
        return entryElements;
    }

    @Override
    public String getPermissionSetAttribute(String permissionSet, String attributename) throws PermissionSetNotFoundException, NoSuchAttributeException {
        Dn dn = SchemaConstants.globalPermissionSetAttribute(permissionSet, attributename);
        Entry entry;
        try {
            entry = dao.lookup(dn);
        } catch (NoSuchNodeException e) {
            throw new NoSuchAttributeException(attributename);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        }
        return TreeStructure.extractAttribute(entry, SchemaConstants.descriptionAttribute);
    }

    @Override
    public void setPermissionSetAttribute(String permissionSet, String attributename, String value) throws PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouGlobalPermissionSetAttributes(permissionSet);
        Entry entry = EntryFactory.namedDescriptiveObject(attributename, value, parent);
        try {
            dao.storeOverwriteExisting(entry);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        }
    }

    @Override
    public Collection<String> getPermissionSetList() {
        Dn parent = SchemaConstants.ouGlobalPermissionSets();
        SearchCursor cursor;
        try {
            cursor = dao.searchOneLevel(parent);
        } catch (NoSuchNodeException e) {
            throw new RuntimeException(e);
        } catch (MissingParentException e) {
            throw new RuntimeException(e);
        }
        return TreeStructure.extractAttribute(cursor, SchemaConstants.cnAttribute);
    }

    @Override
    public Collection<String> getPermissionSetsFromPermissionSet(String permissionSet) throws PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouGlobalPermissionSetChildren(permissionSet);
        SearchCursor cursor;
        try {
            cursor = dao.searchOneLevel(parent);
        } catch (NoSuchNodeException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        }
        return TreeStructure.extractAttribute(cursor, SchemaConstants.cnAttribute);
    }

    @Override
    public Collection<String> getPermissionSetsFromUser(String username) throws UserNotFoundException {
        Dn parent = SchemaConstants.ouUserPermissionSets(username);
        SearchCursor cursor;
        try {
            cursor = dao.searchOneLevel(parent);
        } catch (NoSuchNodeException e) {
            throw new UserNotFoundException(username);
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        }
        return TreeStructure.extractAttribute(cursor, SchemaConstants.cnAttribute);
    }

    @Override
    public void addPermissionSetToUser(String username, String... permissionSet) throws UserNotFoundException, PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouUserPermissionSets(username);
        try {
            addPermissionSets(parent, permissionSet);
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        }
    }

    @Override
    public void addPermissionSetToPermissionSet(String permissionSetParent, String... permissionSet) throws PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouGlobalPermissionSetChildren(permissionSetParent);
        try {
            addPermissionSets(parent, permissionSet);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSetParent);
        }
    }

    private void addPermissionSets(Dn parent, String... permissionSet) throws MissingParentException {

        for(String s : permissionSet){
            if(!dao.exists(SchemaConstants.globalPermissionSet(s))){
                throw new PermissionSetNotFoundException(s);
            }
        }

        String oldMaxId;
        try {
            oldMaxId = dao.updateMaxId(parent, permissionSet.length);
        } catch (MissingOrderException e) {
            throw new RuntimeException(e);
        } catch (NoSuchNodeException e) {
            throw new RuntimeException(e);
        } 

        List<Entry> entries = new LinkedList<Entry>();

        //done in separate loop to provide some atomicity
        for(String s : permissionSet){
            Entry entry = EntryFactory.namedObject(s, parent);
            entries.add(entry);
        }

        OrderFilter.addIds(entries, oldMaxId, false);
        dao.storeSkipExisting(entries);
    }

    @Override
    public void addPermissionToUser(String username, Permission... permissions) throws UserNotFoundException {
        Dn parent = SchemaConstants.ouUserPermissionsDirect(username);
        try {
            addPermissions(parent, permissions);
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        }
    }

    @Override
    public void addPermissionToSet(String permissionSet, Permission... permissions) throws PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouGlobalPermissionsDirect(permissionSet);
        try {
            addPermissions(parent, permissions);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        }
    }

    private void addPermissions(Dn parent, Permission... permission) throws MissingParentException {

        List<Entry> entries = new LinkedList<Entry>();

        for(Permission p : permission){
            Entry entry = EntryFactory.javaObject(p.getClass().getName(), p.describe(), parent);
            entries.add(entry);
        }

        String maxId;
        try {
            maxId = dao.updateMaxId(parent, permission.length);
        } catch (NoSuchNodeException e) {
            throw new RuntimeException(e);
        } catch (MissingOrderException e) {
            throw new RuntimeException(e);
        }

        OrderFilter.addIds(entries, maxId, true);
        dao.storeSkipExisting(entries);
    }

    @Override
    public void createPermissionSet(String permissionSet, Permission... permissions) throws PermissionSetAlreadyExistsException {
        List<EntryElement> entryElements = makeEntryElementListFromPermissions(permissions);
        List<Entry> structure = TreeStructure.globalPermissionSetStructure(permissionSet, entryElements);

        try {
            dao.store(structure);
        } catch (EntryAlreadyExistsException e) {
            throw new PermissionSetAlreadyExistsException();
        } catch (MissingParentException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removePermissionFromSet(String permissionSet, Permission... permission) throws PermissionSetNotFoundException {
        Dn baseDn = SchemaConstants.ouGlobalPermissionsDirect(permissionSet);
        try {
            deletePermission(baseDn, permission);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        }
    }

    @Override
    public void removePermissionFromUser(String username, Permission... permission) throws UserNotFoundException {
        Dn baseDn = SchemaConstants.ouUserPermissionsDirect(username);
        try {
            deletePermission(baseDn, permission);
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        }
    }

    private void deletePermission(Dn baseDn, Permission... permission) throws MissingParentException{
        for(Permission p : permission){
            try {
                //a permission is deleted if it's class and description matches
                dao.deleteSubtreeExcludingRoot(baseDn, String.format("(javaClassName=%s)(description=%s)", p.getClass().getName(), p.describe()));
            } catch (NoSuchNodeException e) {
                throw new RuntimeException(e);
            }    
        }
    }

    @Override
    public void removePermissionSetFromPermissionSet(String permissionSetParent, String... permissionSet) throws PermissionSetNotFoundException {
        for (String child : permissionSet) {
            Dn baseDn = SchemaConstants.globalPermissionChild(permissionSetParent, child);
            try {
                dao.deleteSubtreeIncludingRoot(baseDn);
            } catch (MissingParentException e) {
                throw new PermissionSetNotFoundException(permissionSetParent);
            } catch (NoSuchNodeException e) {
                LOGGER.warn("permissionSet {} was to be deleted, but not found", child);
            }
        }
    }

    @Override
    public void removePermissionSetFromUser(String username, String... permissionSet) throws UserNotFoundException {
        for (String child : permissionSet) {
            Dn baseDn = SchemaConstants.userPermissionSet(username, child);
            try {
                dao.deleteSubtreeIncludingRoot(baseDn);
            } catch (MissingParentException e) {
                throw new UserNotFoundException(username);
            } catch (NoSuchNodeException e) {
                LOGGER.warn("permissionSet {} was to be deleted, but not found", child);
            }
        }
    }

    @Override
    public Collection<String> getUserList() {
        SearchCursor cursor;
        try {
            cursor = dao.searchOneLevel(SchemaConstants.ouUsers());
        } catch (NoSuchNodeException e) {
            throw new RuntimeException(e);
        } catch (MissingParentException e) {
            throw new RuntimeException(e);
        }
        return TreeStructure.extractAttribute(cursor, SchemaConstants.cnAttribute);
    }

    @Override
    public void createUser(String username) throws UserExistsException {
        List<Entry> userStructure = TreeStructure.userStructure(username);
        try {
            dao.store(userStructure);
        } catch (EntryAlreadyExistsException e) {
            throw new UserExistsException();
        } catch (MissingParentException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteUser(String username) {
        try {
            dao.deleteSubtreeIncludingRoot(SchemaConstants.user(username));
        } catch (MissingParentException e) {
            throw new RuntimeException("Inconsistend DIT");
        } catch (NoSuchNodeException e) {
            LOGGER.warn("user {} was to be deleted, but not found", username);
        }
    }

    @Override
    public List<Object> getUserAttribute(String username, String attributename) throws UserNotFoundException, NoSuchAttributeException {
        SearchCursor cursor;
        try {
            cursor = dao.searchOneLevel(SchemaConstants.userAttribute(username, attributename));
        } catch (NoSuchNodeException e) {
            throw new NoSuchAttributeException();
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        }

        List<Entry> entries = OrderFilter.sortById(cursor);
        return TreeStructure.extractUserAttributeValues(entries);
    }

    @Override
    public void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException {

        Entry attribute = EntryFactory.namedObject(attributename, SchemaConstants.ouUserAttributes(username));
        List<Entry> attributeValues = new LinkedList<Entry>();

        for(EntryElement e : EntryUtils.makeEntryElementList(value)){
            Entry entry = EntryFactory.javaObject(e.getType(), e.getValue(), attribute.getDn());
            attributeValues.add(entry);
        }
        OrderFilter.addIds(attributeValues, true);

        try {
            dao.storeOverwriteExisting(attribute);
            for(Entry entry : attributeValues){
                dao.storeSkipExisting(entry);
            }
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        }
    }

    @Override
    public void removeUserAttribute(String username, String attributename) throws UserNotFoundException {

        try {
            dao.deleteSubtreeIncludingRoot(SchemaConstants.userAttribute(username, attributename));
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        } catch (NoSuchNodeException e) {
            LOGGER.warn("attribute {} was to be deleted, but not found", attributename);
        }
    }

    @Override
    public void removeUserCredentials(String username, String credentials) throws UserNotFoundException {
        try {
            dao.deleteSubtreeIncludingRoot(SchemaConstants.userCredentials(username, credentials));
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        } catch (NoSuchNodeException e) {
            LOGGER.warn("credentials {} was to be deleted, but not found", credentials);
        }
    }

    @Override
    public void setUserCredentials(String username, String credentialsType, String credentialsValue) throws UserNotFoundException {
        Dn parent = SchemaConstants.ouUserCredentials(username);
        Entry entry = EntryFactory.namedDescriptiveObject(credentialsType, credentialsValue, parent);
        try {
            dao.storeOverwriteExisting(entry);
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        }
    }


    @Override
    public String getUserCredentials(String username, String credentials) throws UserNotFoundException, NoSuchCredentialsException {
        try {
            Entry entry = dao.lookup(SchemaConstants.userCredentials(username, credentials));
            return TreeStructure.extractAttribute(entry, SchemaConstants.descriptionAttribute);
        } catch (NoSuchNodeException e) {
            throw new NoSuchCredentialsException();
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        }    
    }









    @Override
    public Collection<Permission> getAllPermissionsForUser(String arg0) throws UserNotFoundException {
        // TODO 
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Permission> Collection<T> getAllPermissionsForUser(String arg0, Class<T> arg1) throws UserNotFoundException {
        // TODO 
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Permission> getPermissionsForUser(String arg0) throws UserNotFoundException {
        // TODO 
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Permission> Collection<T> getPermissionsForUser(String arg0, Class<T> arg1) throws UserNotFoundException {
        // TODO 
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Permission> getAllPermissionsFromPermissionSet(String arg0) throws PermissionSetNotFoundException {
        // TODO 
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Permission> getPermissionsFromPermissionSet(String permissionSet) throws PermissionSetNotFoundException {

        //TODO

        //        List<Permission> permissions = new LinkedList<Permission>();
        //        SearchCursor cursor;
        //        
        //        try {
        //            cursor = dao.searchOneLevel(SchemaConstants.ouGlobalPermissionsDirect(permissionSet));
        //        } catch (NoSuchNodeException e) {
        //            throw new RuntimeException(e);
        //        } catch (MissingParentException e) {
        //            throw new PermissionSetNotFoundException();
        //        }
        //        
        //        List<Entry> entries = OrderFilter.sortById(cursor);
        //        
        //        
        //        
        //        return permissions;
        throw new UnsupportedOperationException();
    }





}
