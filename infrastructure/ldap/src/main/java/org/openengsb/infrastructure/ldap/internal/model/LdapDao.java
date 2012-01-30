package org.openengsb.infrastructure.ldap.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.entry.Value;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.message.AddRequest;
import org.apache.directory.shared.ldap.model.message.AddRequestImpl;
import org.apache.directory.shared.ldap.model.message.LdapResult;
import org.apache.directory.shared.ldap.model.message.ModifyRequest;
import org.apache.directory.shared.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.shared.ldap.model.message.ModifyResponse;
import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.model.name.Dn;

public class LdapDao {
    
    private LdapConnection connection;
    
    private String sizeAttribute = "";//TODO
    private String idAttribute = "";

    public LdapResult store(Entry entry) throws LdapException {
        AddRequest addRequest = new AddRequestImpl().setEntry(entry);
        return connection.add(addRequest).getLdapResult();
    } 

    public List<LdapResult> store(Entry... entries) throws LdapException {
        List<LdapResult> results = new ArrayList<LdapResult>(entries.length);
        for(Entry e : entries){
            results.add(store(e));
        }
        return results;
    }
    
    public List<String> getAttributeValues(Entry entry, String attributeType) throws LdapException{

        Attribute attribute = entry.get(attributeType);
        if(attribute == null){
            return Collections.emptyList();
        }

        List<String> values = new LinkedList<String>();
        for(Value<?> value : attribute){
            values.add(value.getString());
        }
        return values;
    }
    
    private String getMaxId(Dn containerDn) throws LdapException, MissingOrderException {
        Entry container = connection.lookup(containerDn, sizeAttribute);
        try {
            return getAttributeValues(container, sizeAttribute).get(0);
        } catch (IndexOutOfBoundsException e) {
            throw new MissingOrderException();
        }
    }

    private void setMaxId(Dn containerDn, String maxId) throws LdapException, MissingOrderException{
        ModifyRequest modifyRequest = new ModifyRequestImpl();
        modifyRequest.setName(containerDn);
        modifyRequest.replace(sizeAttribute, maxId);
        ModifyResponse modifyResponse = connection.modify(modifyRequest);
        if (modifyResponse.getLdapResult().getResultCode() == ResultCodeEnum.NO_SUCH_ATTRIBUTE) {
            throw new MissingOrderException();
        }
    }

}
