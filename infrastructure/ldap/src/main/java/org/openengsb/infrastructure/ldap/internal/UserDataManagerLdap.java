package org.openengsb.infrastructure.ldap.internal;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.DefaultAttribute;
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
import org.openengsb.core.common.util.CollectionUtilsExtended;
import org.openengsb.core.security.internal.EntryUtils;
import org.openengsb.core.security.internal.model.EntryElement;
import org.openengsb.infrastructure.ldap.internal.model.EntryAlreadyExistsException;
import org.openengsb.infrastructure.ldap.internal.model.EntryFactory;
import org.openengsb.infrastructure.ldap.internal.model.MissingParentException;
import org.openengsb.infrastructure.ldap.internal.model.NoSuchNodeException;
import org.openengsb.infrastructure.ldap.internal.model.OrderFilter;
import org.openengsb.infrastructure.ldap.internal.model.SchemaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.ComputationException;


public class UserDataManagerLdap implements UserDataManager {

    private LdapDao dao;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataManagerLdap.class);

    public void setLdapDao(LdapDao dao){
        this.dao = dao;
    }

    public LdapDao getDao(){
        return dao;
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
        return LdapUtils.extractFirstValueOfAttribute(entry, SchemaConstants.stringAttribute);
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
        SearchCursor cursor = dao.searchOneLevel(parent);
        return LdapUtils.extractFirstValueOfAttribute(cursor, SchemaConstants.cnAttribute);
    }

    @Override
    public Collection<String> getPermissionSetsFromPermissionSet(String permissionSet) throws PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouGlobalPermissionSetChildren(permissionSet);
        SearchCursor cursor;
        try {
            cursor = dao.searchOneLevel(parent);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        }
        //TODO here we can also restore order!
        return LdapUtils.extractFirstValueOfAttribute(cursor, SchemaConstants.cnAttribute);
    }

    @Override
    public Collection<String> getPermissionSetsFromUser(String username) throws UserNotFoundException {
        Dn parent = SchemaConstants.ouUserPermissionSets(username);
        SearchCursor cursor;
        try {
            cursor = dao.searchOneLevel(parent);
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        }
        return LdapUtils.extractFirstValueOfAttribute(cursor, SchemaConstants.cnAttribute);
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

        List<Entry> entries = new LinkedList<Entry>();
        for(String s : permissionSet){ //done in separate loop to provide some atomicity
            Entry entry = EntryFactory.namedObject(s, parent);
            entries.add(entry);
        }

        String oldMaxId = updateMaxId(parent, permissionSet.length);
        OrderFilter.addIds(entries, oldMaxId, false);
        dao.storeOverwriteExisting(entries);
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
        String maxId = updateMaxId(parent, permission.length);
        OrderFilter.addIds(entries, maxId, true);
        dao.storeSkipExisting(entries);
    }

    @Override
    public void createPermissionSet(String permissionSet, Permission... permissions) throws PermissionSetAlreadyExistsException {

        List<Entry> structure = globalPermissionSetStructure(permissionSet, permissions);
        try {
            dao.store(structure);
        } catch (EntryAlreadyExistsException e) {
            throw new PermissionSetAlreadyExistsException();
        }
    }

    private List<Entry> globalPermissionSetStructure(String permissionSet, Permission... permissions){

        List<Entry> entries = new LinkedList<Entry>();
        List<Entry> permissionEntries = new LinkedList<Entry>();
        Dn parent = SchemaConstants.ouGlobalPermissionSets();

        Entry permissionSetEntry = EntryFactory.namedObject(permissionSet, parent);
        Entry ouDirect = EntryFactory.organizationalUnit("direct", permissionSetEntry.getDn());
        Entry ouChildrenSets = EntryFactory.organizationalUnit("childrenSets", permissionSetEntry.getDn());
        Entry ouAttributes = EntryFactory.organizationalUnit("attributes", permissionSetEntry.getDn());

        for(Permission p : permissions){
            permissionEntries.add(EntryFactory.javaObject(p.getClass().getName(), p.describe(), ouDirect.getDn()));
        }

        OrderFilter.addIds(permissionEntries, true);
        OrderFilter.makeContainerAware(ouDirect, String.valueOf(permissions.length));
        OrderFilter.makeContainerAware(ouChildrenSets);

        entries.add(permissionSetEntry);
        entries.add(ouAttributes);
        entries.add(ouDirect);
        entries.add(ouChildrenSets);
        entries.addAll(permissionEntries);

        return entries;
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

    private void deletePermission(Dn baseDn, Permission... permission) {
        for(Permission p : permission){
            //a permission is deleted if it's class and description matches
            dao.deleteMatchingChildren(baseDn, String.format("(javaClassName=%s)(openengsb-string=%s)", p.getClass().getName(), p.describe()));
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

    //TODO make missing node exception superclass of missing parent exception. makes sense!

    @Override
    public Collection<Permission> getPermissionsForUser(String username) throws UserNotFoundException {
        SearchCursor cursor;
        try {
            cursor = dao.searchOneLevel(SchemaConstants.ouUserPermissionsDirect(username));
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        }
        List<Entry> entries = OrderFilter.sortById(cursor);
        return extractPermissions(entries);
    }

    private Collection<Permission> extractPermissions(List<Entry> entries){
        Collection<Permission> permissions = new LinkedList<Permission>();
        for(Entry entry : entries){
            String type = LdapUtils.extractFirstValueOfAttribute(entry, SchemaConstants.javaClassNameAttribute);
            String value = LdapUtils.extractFirstValueOfAttribute(entry, SchemaConstants.stringAttribute);
            Class<?> elementType;
            try {
                elementType = Class.forName(type);
            } catch (ClassNotFoundException e) {
                throw new ComputationException(e);
            }
            try {
                Constructor<?> constructor = elementType.getConstructor(String.class);
                permissions.add((Permission)constructor.newInstance(value));
            } catch (Exception e) {
                ReflectionUtils.handleReflectionException(e);
                throw new ComputationException(e);
            }
        }
        return permissions;
    }

    @Override
    public <T extends Permission> Collection<T> getPermissionsForUser(String username, Class<T> type) throws UserNotFoundException {
        //TODO improve performance by better query
        return CollectionUtilsExtended.filterCollectionByClass(getPermissionsForUser(username), type);
    }

    @Override
    public Collection<Permission> getPermissionsFromPermissionSet(String permissionSet) throws PermissionSetNotFoundException {
        SearchCursor cursor;
        try {
            cursor = dao.searchOneLevel(SchemaConstants.ouGlobalPermissionsDirect(permissionSet));
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        }
        List<Entry> entries = OrderFilter.sortById(cursor);
        return extractPermissions(entries);
    }

    @Override
    public Collection<Permission> getAllPermissionsForUser(String username) throws UserNotFoundException {
        Collection<Permission> result = new HashSet<Permission>();
        result.addAll(getPermissionsForUser(username));
        for(String s : getPermissionSetsFromUser(username)){
            result.addAll(getAllPermissionsFromPermissionSet(s));
        }
        return result;
    }

    @Override
    public <T extends Permission> Collection<T> getAllPermissionsForUser(String username, Class<T> type) throws UserNotFoundException {
        //TODO improve performance by better query
        return CollectionUtilsExtended.filterCollectionByClass(getPermissionsForUser(username), type);
    }

    //TODO note: this does not reflect insertion order because HashSet is used

    @Override
    public Collection<Permission> getAllPermissionsFromPermissionSet(String permissionSet) throws PermissionSetNotFoundException {
        Collection<Permission> result = new HashSet<Permission>();
        for(String s : getAllPermissionSetsFromPermissionSet(permissionSet)){
            result.addAll(getPermissionsFromPermissionSet(s));
        }
        return result;
    }

    private Collection<String> getAllPermissionSetsFromPermissionSet(String permissionSet){
        Collection<String> result = new HashSet<String>();
        for(String s : getPermissionSetsFromPermissionSet(permissionSet)){
            boolean b = result.addAll(getAllPermissionSetsFromPermissionSet(s));
            if(!b){ //prevents circles
                break;
            }
        }        
        return result;
    }


    @Override
    public Collection<String> getUserList() {
        SearchCursor cursor = dao.searchOneLevel(SchemaConstants.ouUsers());
        return LdapUtils.extractFirstValueOfAttribute(cursor, SchemaConstants.cnAttribute);
    }

    @Override
    public void createUser(String username) throws UserExistsException {
        List<Entry> userStructure = userStructure(username);
        try {
            dao.store(userStructure);
        } catch (EntryAlreadyExistsException e) {
            throw new UserExistsException();
        }
    }

    @Override
    public void deleteUser(String username) {
        try {
            dao.deleteSubtreeIncludingRoot(SchemaConstants.user(username));
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
        return extractUserAttributeValues(entries);
    }

    private List<Object> extractUserAttributeValues(List<Entry> entries){
        List<EntryElement> entryElements = new LinkedList<EntryElement>();
        for(Entry entry : entries){
            String type = LdapUtils.extractFirstValueOfAttribute(entry, SchemaConstants.javaClassNameAttribute);
            String value = LdapUtils.extractFirstValueOfAttribute(entry, SchemaConstants.stringAttribute);
            entryElements.add(new EntryElement(type, value));
        }
        return EntryUtils.convertAllEntryElementsToObject(entryElements);
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
            return LdapUtils.extractFirstValueOfAttribute(entry, SchemaConstants.stringAttribute);
        } catch (NoSuchNodeException e) {
            throw new NoSuchCredentialsException();
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        }    
    }

    private List<Entry> userStructure(String username) {

        List<Entry> entries = new LinkedList<Entry>();

        Entry entry = EntryFactory.namedObject(username, SchemaConstants.ouUsers());
        Entry ouPermissions = EntryFactory.organizationalUnit("permissions", entry.getDn());
        Entry ouDirectPermissions = EntryFactory.organizationalUnit("direct", ouPermissions.getDn());
        Entry ouPermissionSets = EntryFactory.organizationalUnit("permissionSets", ouPermissions.getDn());

        OrderFilter.makeContainerAware(ouDirectPermissions);
        OrderFilter.makeContainerAware(ouPermissionSets);

        entries.add(entry);
        entries.add(ouPermissions);
        entries.add(ouDirectPermissions);
        entries.add(ouPermissionSets);
        entries.add(EntryFactory.organizationalUnit("credentials", entry.getDn()));
        entries.add(EntryFactory.organizationalUnit("attributes", entry.getDn()));

        return entries;
    }

    /**
     * @return the maxId before the update
     */
    private String updateMaxId(Dn containerDn, int additionalItems) {
        String oldMaxId = lookupMaxId(containerDn);
        String newMaxId = OrderFilter.calculateNewMaxId(oldMaxId, additionalItems);
        setMaxId(containerDn, newMaxId);
        return oldMaxId;
    }

    private void setMaxId(Dn containerDn, String maxId) {
        Attribute attribute = new DefaultAttribute(OrderFilter.maxIdAttribute, maxId);
        dao.modify(containerDn, attribute);
    }

    private String lookupMaxId(Dn containerDn) {
        Entry entry = dao.lookup(containerDn);
        return LdapUtils.extractFirstValueOfAttribute(entry, OrderFilter.maxIdAttribute);
    }

}
