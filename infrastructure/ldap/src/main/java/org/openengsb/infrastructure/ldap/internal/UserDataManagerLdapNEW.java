package org.openengsb.infrastructure.ldap.internal;

import java.util.Collection;
import java.util.List;

import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapException;
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
import org.openengsb.infrastructure.ldap.internal.model.EntryAlreadyExistsException;
import org.openengsb.infrastructure.ldap.internal.model.LdapDao;
import org.openengsb.infrastructure.ldap.internal.model.NoSuchObjectException;
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





    @Override
    public List<Object> getUserAttribute(String arg0, String arg1) throws UserNotFoundException, NoSuchAttributeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUserCredentials(String arg0, String arg1) throws UserNotFoundException, NoSuchCredentialsException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getUserList() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void addPermissionSetToPermissionSet(String arg0, String... arg1) throws PermissionSetNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPermissionSetToUser(String arg0, String... arg1) throws UserNotFoundException, PermissionSetNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPermissionToSet(String arg0, Permission... arg1) throws PermissionSetNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPermissionToUser(String arg0, Permission... arg1) throws UserNotFoundException {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void createPermissionSet(String permissionSet, Permission... permission) throws PermissionSetAlreadyExistsException {
        List<Entry> structure = TreeStructure.globalPermissionSetStructure(permissionSet, permission);
        try {
            dao.store(structure);
        } catch (EntryAlreadyExistsException e) {
            throw new PermissionSetAlreadyExistsException();
        } catch (NoSuchObjectException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void createUser(String username) throws UserExistsException {
        List<Entry> userStructure = TreeStructure.userStructure(username);
        try {
            dao.store(userStructure);
        } catch (EntryAlreadyExistsException e) {
            throw new UserExistsException();
        } catch (NoSuchObjectException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setUserAttribute(String username, String attributename, Object... value) throws UserNotFoundException {
        List<EntryElement> entryElements = EntryUtils.makeEntryElementList(value);
        List<Entry> structure = TreeStructure.userAttributeStructure(username, attributename, entryElements);
        try {
            dao.storeOverwriteExisting(structure);
        } catch (NoSuchObjectException e) {
            throw new UserNotFoundException(e);
        }
    }

    @Override
    public void removeUserAttribute(String username, String attributename) throws UserNotFoundException {
        Dn attributeDn = TreeStructure.userAttribute(username, attributename);
        Dn userDn = TreeStructure.user(username);
        
        try {
            if(dao.getConnection().exists(userDn)){//TODO hide this from usermanager. getMatched(), y u no work!!1 its quite easy to implement myself.. but still annoying
                try {
                    dao.deleteSubtreeIncludingRoot(attributeDn);
                } catch (NoSuchObjectException e) {
                    LOGGER.warn("attribute {} was to be deleted, but not found", attributename);      
                }
            } else{
                throw new UserNotFoundException();
            }
        } catch (LdapException e) {
            throw new RuntimeException();
        }
    }
    
    @Override
    public void deleteUser(String username) {
        Dn dn = TreeStructure.user(username);
        try {
            dao.deleteSubtreeIncludingRoot(dn);
        } catch (NoSuchObjectException e) {
            LOGGER.warn("user {} was to be deleted, but not found", username);
        }
    }


    @Override
    public void removeUserCredentials(String arg0, String arg1) throws UserNotFoundException {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void setUserCredentials(String username, String credentialsType, String credentialsValue) throws UserNotFoundException {
        Entry entry = TreeStructure.credentialsEntry(username, credentialsType, credentialsValue);
        try {
            dao.storeOverwriteExisting(entry);
        } catch (NoSuchObjectException e) {
            throw new UserNotFoundException();
        }

    }
    
    @Override
    public Collection<Permission> getAllPermissionsForUser(String arg0) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Permission> Collection<T> getAllPermissionsForUser(String arg0, Class<T> arg1) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Permission> getAllPermissionsFromPermissionSet(String arg0) throws PermissionSetNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPermissionSetAttribute(String arg0, String arg1) throws PermissionSetNotFoundException, NoSuchAttributeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getPermissionSetList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getPermissionSetsFromPermissionSet(String arg0) throws PermissionSetNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getPermissionSetsFromUser(String arg0) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Permission> getPermissionsForUser(String arg0) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Permission> Collection<T> getPermissionsForUser(String arg0, Class<T> arg1) throws UserNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Permission> getPermissionsFromPermissionSet(String arg0) throws PermissionSetNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public void removePermissionFromSet(String arg0, Permission... arg1) throws PermissionSetNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermissionFromUser(String arg0, Permission... arg1) throws UserNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermissionSetFromPermissionSet(String arg0, String... arg1) throws PermissionSetNotFoundException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermissionSetFromUser(String arg0, String... arg1) throws UserNotFoundException {
        // TODO Auto-generated method stub

    }




    @Override
    public void setPermissionSetAttribute(String arg0, String arg1, String arg2) throws PermissionSetNotFoundException {
        // TODO Auto-generated method stub

    }





}
