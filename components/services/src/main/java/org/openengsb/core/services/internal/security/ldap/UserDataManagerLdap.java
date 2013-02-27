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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.openengsb.core.services.internal.security.EntryUtils;
import org.openengsb.core.services.internal.security.PermissionUtils;
import org.openengsb.core.services.internal.security.model.EntryElement;
import org.openengsb.core.services.internal.security.model.EntryValue;
import org.openengsb.core.services.internal.security.model.PermissionData;
import org.openengsb.core.util.CollectionUtilsExtended;
import org.openengsb.infrastructure.ldap.EntryAlreadyExistsException;
import org.openengsb.infrastructure.ldap.InconsistentDITException;
import org.openengsb.infrastructure.ldap.LdapDao;
import org.openengsb.infrastructure.ldap.MissingParentException;
import org.openengsb.infrastructure.ldap.NoSuchNodeException;
import org.openengsb.infrastructure.ldap.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class UserDataManagerLdap implements UserDataManager {

    private LdapDao dao;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataManagerLdap.class);

    public void setLdapDao(LdapDao dao) {
        this.dao = dao;
    }

    public LdapDao getDao() {
        return dao;
    }

    @Override
    public String getPermissionSetAttribute(String permissionSet, String attributename)
        throws PermissionSetNotFoundException, NoSuchAttributeException {
        Dn dn = SchemaConstants.globalPermissionSetAttribute(permissionSet, attributename);
        Entry entry;
        try {
            entry = dao.lookup(dn);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        } catch (NoSuchNodeException e) {
            throw new NoSuchAttributeException(attributename);
        }
        return LdapUtils.extractAttributeEmptyCheck(entry, SchemaConstants.STRING_ATTRIBUTE);
    }

    @Override
    public void setPermissionSetAttribute(String permissionSet, String attributename, String value)
        throws PermissionSetNotFoundException {
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
        List<Entry> entries;
        try {
            entries = dao.getDirectChildren(parent);
        } catch (InconsistentDITException e) {
            throw new LdapRuntimeException(e);
        }
        return LdapUtils.extractAttributeEmptyCheck(entries, SchemaConstants.CN_ATTRIBUTE);
    }

    @Override
    public Collection<String> getPermissionSetsFromPermissionSet(String permissionSet)
        throws PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouGlobalPermissionSetChildren(permissionSet);
        List<Entry> entries;
        try {
            entries = dao.getDirectChildren(parent);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        } catch (NoSuchNodeException e) {
            throw new LdapRuntimeException(e);
        }
        TimebasedOrderFilter.sortById(entries);
        return LdapUtils.extractAttributeEmptyCheck(entries, SchemaConstants.CN_ATTRIBUTE);
    }

    @Override
    public Collection<String> getPermissionSetsFromUser(String username) throws UserNotFoundException {
        Dn parent = SchemaConstants.ouUserPermissionSets(username);
        List<Entry> entries;
        try {
            entries = dao.getDirectChildren(parent);
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        } catch (NoSuchNodeException e) {
            throw new LdapRuntimeException(e);
        }
        TimebasedOrderFilter.sortById(entries);
        return LdapUtils.extractAttributeEmptyCheck(entries, SchemaConstants.CN_ATTRIBUTE);
    }

    @Override
    public void addPermissionSetToUser(String username, String... permissionSet) throws UserNotFoundException,
            PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouUserPermissionSets(username);
        try {
            storePermissionSets(parent, permissionSet);
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        }
    }

    @Override
    public void addPermissionSetToPermissionSet(String permissionSetParent, String... permissionSet)
        throws PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouGlobalPermissionSetChildren(permissionSetParent);
        try {
            storePermissionSets(parent, permissionSet);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSetParent);
        }
    }

    private void storePermissionSets(Dn parent, String... permissionSet) throws MissingParentException {
        for (String s : permissionSet) {
            if (!dao.exists(SchemaConstants.globalPermissionSet(s))) {
                throw new PermissionSetNotFoundException(s);
            }
        }
        List<Entry> entries = new LinkedList<Entry>();
        for (String s : permissionSet) { // done in separate loop to provide
            // some atomicity
            Entry entry = EntryFactory.namedObject(s, parent);
            TimebasedOrderFilter.addId(entry, false);
            entries.add(entry);
        }
        dao.storeSkipExisting(entries);
    }

    @Override
    public void addPermissionToUser(String username, Permission... permissions) throws UserNotFoundException {
        Dn parent = SchemaConstants.ouUserPermissionsDirect(username);
        try {
            storePermissions(parent, permissions);
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        } catch (EntryAlreadyExistsException e) {
            throw new LdapRuntimeException(e); //TODO also here duplicates should be allowed but this looks like not
        }
    }

    @Override
    public void addPermissionToSet(String permissionSet, Permission... permissions)
        throws PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouGlobalPermissionsDirect(permissionSet);
        try {
            storePermissions(parent, permissions);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        } catch (EntryAlreadyExistsException e) {
            throw new LdapRuntimeException(e); //TODO here is it possible to store duplicates? think not..
        }
    }

    @Override
    public void createPermissionSet(String permissionSet, Permission... permission)
        throws PermissionSetAlreadyExistsException {
        List<Entry> permissionSetStructure = EntryFactory.globalPermissionSetStructure(permissionSet);
        try {
            dao.store(permissionSetStructure);
            storePermissions(SchemaConstants.ouGlobalPermissionsDirect(permissionSet), permission);
        } catch (EntryAlreadyExistsException e) {
            throw new PermissionSetAlreadyExistsException();
        } catch (MissingParentException e) {
            throw new LdapRuntimeException(e);
        }
    }

    private void storePermissions(Dn parent, Permission... permission) throws EntryAlreadyExistsException,
            MissingParentException {
        if (permission == null) {
            return;
        }
        Collection<PermissionData> pd = new LinkedList<PermissionData>();
        for (Permission p : permission) {
            PermissionData data = PermissionUtils.convertPermissionToPermissionData(p);
            pd.add(data);
        }
        List<Entry> permissionStructure = EntryFactory.permissionStructureFromPermissionData(pd, parent);
        dao.store(permissionStructure);
    }

    @Override
    public void removePermissionFromSet(String permissionSet, Permission... permission)
        throws PermissionSetNotFoundException {
        Dn parent = SchemaConstants.ouGlobalPermissionsDirect(permissionSet);
        try {
            deletePermission(parent, permission);
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        } catch (NoSuchNodeException e) {
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public void removePermissionFromUser(String username, Permission... permission) throws UserNotFoundException {
        Dn parent = SchemaConstants.ouUserPermissionsDirect(username);
        try {
            deletePermission(parent, permission);
        } catch (MissingParentException e) {
            throw new UserNotFoundException(username);
        } catch (NoSuchNodeException e) {
            throw new LdapRuntimeException(e);
        }
    }

    private void deletePermission(Dn parent, Permission... permission) throws MissingParentException,
            NoSuchNodeException {
        List<Node> nodes = dao.searchSubtree(parent);
        Collection<Permission> permissions = extractPermissionsFromNodes(nodes);
        boolean b = permissions.removeAll(Arrays.asList(permission));
        if (b) {
            dao.deleteSubtreeExcludingRoot(parent);
            try {
                storePermissions(parent, permissions.toArray(new Permission[0]));
            } catch (EntryAlreadyExistsException e) {
                LOGGER.debug("should never reach here");
                throw new LdapRuntimeException(e);
            }
        }
    }

    @Override
    public void removePermissionSetFromPermissionSet(String permissionSetParent, String... permissionSet)
        throws PermissionSetNotFoundException {
        for (String child : permissionSet) {
            Dn parent = SchemaConstants.globalPermissionChild(permissionSetParent, child);
            try {
                dao.deleteSubtreeIncludingRoot(parent);
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
    public Collection<Permission> getPermissionsForUser(String username) throws UserNotFoundException {
        List<Node> nodes;
        try {
            nodes = dao.searchSubtree(SchemaConstants.ouUserPermissionsDirect(username));
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        } catch (NoSuchNodeException e) {
            throw new LdapRuntimeException(e);
        }
        return extractPermissionsFromNodes(nodes);
    }

    private Collection<Permission> extractPermissionsFromNodes(List<Node> nodes) {
        Collection<PermissionData> permissionData = new LinkedList<PermissionData>();
        TimebasedOrderFilter.sortByIdNode(nodes);
        for (Node permission : nodes) {
            Map<String, EntryValue> attributes = Maps.newHashMap();
            for (Node property : permission.getChildren()) {

                List<EntryElement> entryElements = new LinkedList<EntryElement>();
                TimebasedOrderFilter.sortByIdNode(property.getChildren());
                for (Node propertyValue : property.getChildren()) {
                    EntryElement entryElement = new EntryElement();
                    entryElement.setType(LdapUtils.extractAttributeEmptyCheck(propertyValue.getEntry(),
                            SchemaConstants.JAVA_CLASS_NAME_ATTRIBUTE));
                    entryElement.setValue(LdapUtils.extractAttributeEmptyCheck(propertyValue.getEntry(),
                            SchemaConstants.STRING_ATTRIBUTE));
                    entryElements.add(entryElement);
                }
                String key = LdapUtils.extractAttributeEmptyCheck(property.getEntry(), SchemaConstants.CN_ATTRIBUTE);
                EntryValue entryValue = new EntryValue(key, entryElements);
                attributes.put(key, entryValue); // TODO why need key 2x?
            }
            String type = LdapUtils.extractAttributeEmptyCheck(permission.getEntry(),
                    SchemaConstants.JAVA_CLASS_NAME_ATTRIBUTE);
            PermissionData data = new PermissionData();
            data.setType(type);
            data.setAttributes(attributes);
            permissionData.add(data);
        }
        return EntryUtils.convertAllBeanDataToObjects(permissionData);
    }

    @Override
    public <T extends Permission> Collection<T> getPermissionsForUser(String username, Class<T> type)
        throws UserNotFoundException {
        // TODO improve performance by better query
        return CollectionUtilsExtended.filterCollectionByClass(getPermissionsForUser(username), type);
    }

    @Override
    public Collection<Permission> getPermissionsFromPermissionSet(String permissionSet)
        throws PermissionSetNotFoundException {
        List<Node> nodes;
        try {
            nodes = dao.searchSubtree(SchemaConstants.ouGlobalPermissionsDirect(permissionSet));
        } catch (MissingParentException e) {
            throw new PermissionSetNotFoundException(permissionSet);
        } catch (NoSuchNodeException e) {
            throw new LdapRuntimeException(e);
        }
        return extractPermissionsFromNodes(nodes);
    }

    @Override
    public Collection<Permission> getAllPermissionsForUser(String username) throws UserNotFoundException {
        Collection<Permission> result = new HashSet<Permission>();
        result.addAll(getPermissionsForUser(username));
        for (String s : getPermissionSetsFromUser(username)) {
            result.addAll(getAllPermissionsFromPermissionSet(s));
        }
        return result;
    }

    @Override
    public <T extends Permission> Collection<T> getAllPermissionsForUser(String username, Class<T> type)
        throws UserNotFoundException {
        // TODO improve performance by better query
        return CollectionUtilsExtended.filterCollectionByClass(getPermissionsForUser(username), type);
    }

    // TODO note: this does not reflect insertion order because HashSet is used

    @Override
    public Collection<Permission> getAllPermissionsFromPermissionSet(String permissionSet)
        throws PermissionSetNotFoundException {
        Collection<Permission> result = new HashSet<Permission>();
        for (String s : getAllPermissionSetsFromPermissionSet(permissionSet)) {
            result.addAll(getPermissionsFromPermissionSet(s));
        }
        return result;
    }

    private Collection<String> getAllPermissionSetsFromPermissionSet(String permissionSet) {
        Collection<String> result = new HashSet<String>();
        for (String s : getPermissionSetsFromPermissionSet(permissionSet)) {
            boolean b = result.addAll(getAllPermissionSetsFromPermissionSet(s));
            if (!b) { // prevents circles
                break;
            }
        }
        return result;
    }

    @Override
    public Collection<String> getUserList() {
        List<Entry> entries;
        try {
            entries = dao.getDirectChildren(SchemaConstants.ouUsers());
        } catch (NoSuchNodeException e) {
            throw new LdapRuntimeException(e);
        } catch (MissingParentException e) {
            throw new LdapRuntimeException(e);
        }
        return LdapUtils.extractAttributeEmptyCheck(entries, SchemaConstants.CN_ATTRIBUTE);
    }

    @Override
    public void createUser(String username) throws UserExistsException {
        List<Entry> userStructure = userStructure(username);
        try {
            dao.store(userStructure);
        } catch (EntryAlreadyExistsException e) {
            throw new UserExistsException();
        } catch (MissingParentException e) {
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public void deleteUser(String username) {
        try {
            dao.deleteSubtreeIncludingRoot(SchemaConstants.user(username));
        } catch (NoSuchNodeException e) {
            LOGGER.warn("user {} was to be deleted, but not found", username);
        } catch (MissingParentException e) {
            throw new LdapRuntimeException(e);
        }
    }

    @Override
    public List<Object> getUserAttribute(String username, String attributename) throws UserNotFoundException,
            NoSuchAttributeException {
        List<Entry> entries;
        try {
            entries = dao.getDirectChildren(SchemaConstants.userAttribute(username, attributename));
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        } catch (NoSuchNodeException e) {
            throw new NoSuchAttributeException();
        }
        TimebasedOrderFilter.sortById(entries);
        return extractUserAttributeValues(entries);
    }

    private List<Object> extractUserAttributeValues(List<Entry> entries) {
        List<EntryElement> entryElements = new LinkedList<EntryElement>();
        for (Entry entry : entries) {
            String type = LdapUtils.extractAttributeEmptyCheck(entry, SchemaConstants.JAVA_CLASS_NAME_ATTRIBUTE);
            String value = LdapUtils.extractAttributeEmptyCheck(entry, SchemaConstants.STRING_ATTRIBUTE);
            entryElements.add(new EntryElement(type, value));
        }
        return EntryUtils.convertAllEntryElementsToObject(entryElements);
    }

    @Override
    public void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException {

        Entry attribute = EntryFactory.namedObject(attributename, SchemaConstants.ouUserAttributes(username));
        List<Entry> attributeValues = new LinkedList<Entry>();

        for (EntryElement e : EntryUtils.makeEntryElementList(value)) {
            Entry entry = EntryFactory.javaObject(e.getType(), e.getValue(), attribute.getDn());
            attributeValues.add(entry);
        }
        TimebasedOrderFilter.addIds(attributeValues, true);

        try {
            dao.storeOverwriteExisting(attribute);
            for (Entry entry : attributeValues) {
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
    public void setUserCredentials(String username, String credentialsType, String credentialsValue)
        throws UserNotFoundException {
        Dn parent = SchemaConstants.ouUserCredentials(username);
        Entry entry = EntryFactory.namedDescriptiveObject(credentialsType, credentialsValue, parent);
        try {
            dao.storeOverwriteExisting(entry);
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        }
    }

    @Override
    public String getUserCredentials(String username, String credentials) throws UserNotFoundException,
            NoSuchCredentialsException {
        try {
            Entry entry = dao.lookup(SchemaConstants.userCredentials(username, credentials));
            return LdapUtils.extractAttributeEmptyCheck(entry, SchemaConstants.STRING_ATTRIBUTE);
        } catch (MissingParentException e) {
            throw new UserNotFoundException();
        } catch (NoSuchNodeException e) {
            throw new NoSuchCredentialsException();
        }
    }

    private List<Entry> userStructure(String username) {
        Entry entry = EntryFactory.namedObject(username, SchemaConstants.ouUsers());
        Entry ouPermissions = EntryFactory.organizationalUnit("permissions", entry.getDn());
        Entry ouDirectPermissions = EntryFactory.organizationalUnit("direct", ouPermissions.getDn());
        Entry ouPermissionSets = EntryFactory.organizationalUnit("permissionSets", ouPermissions.getDn());
        Entry ouCredentials = EntryFactory.organizationalUnit("credentials", entry.getDn());
        Entry ouAttributes = EntryFactory.organizationalUnit("attributes", entry.getDn());
        return Arrays.asList(entry, ouPermissions, ouDirectPermissions, ouPermissionSets, ouCredentials, ouAttributes);
    }

}
