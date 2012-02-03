package org.openengsb.infrastructure.ldap.internal.model;

import java.math.BigInteger;
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
import org.apache.directory.shared.ldap.model.name.Rdn;

public class StorageOLD {

    private LdapConnection connection;

    private String sizeAttribute = "";//TODO
    private String idAttribute = "";

    public StorageOLD(LdapConnection connection) {
        this.connection = connection;
    }

    private List<String> getAttributeValues(Entry entry, String attributeType) throws LdapException{

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
    
//    public List<LdapResult> storeOrdered(Entry[] entries, boolean aware, boolean allowDuplicates)
//            throws LdapException, MissingOrderException {
//        
//        List<String> generatedIdSequence;
//        
//        if(aware){
//            Dn parentDn = entries[0].getDn().getParent();
//            String currentMaxId = getMaxId(parentDn);
//            generatedIdSequence = generateIdSequence(currentMaxId, entries.length);
//            setMaxId(parentDn, generatedIdSequence.get(generatedIdSequence.size()-1));
//        }else{
//            generatedIdSequence = generateIdSequence(entries.length);
//        }
//        
//        addOrderingInformation(entries, generatedIdSequence, allowDuplicates);
//        
//        return store(entries);
//    }

    /**
     * If the rdn should not change, set updateRdn to false. It is only necessary to change
     * the rdn if the container allows duplicates. Otherwise the ordering information will
     * be added like any other secondary attribute.
     * @throws LdapException
     * */
    private void addOrderingInformation(final Entry[] entries, List<String> idSequence, boolean updateRdn)
            throws LdapException{

        Entry entry;
        String id;
        
        for(int i = 0; i<entries.length; i++){
            entry = entries[i];
            id = idSequence.get(i);
            
            entry.add(idAttribute, id);
            if(updateRdn){
                Rdn rdn = new Rdn(String.format("%s=%s", idAttribute, id));
                entry.setDn(new Dn(rdn,entry.getDn().getParent()));
            }
        }
    }
    
//    /**
//     * Creates a sequence with start value 1.<br>
//     * The first element has id == 1.<br>
//     * The last element has id == additionalItems.<br>
//     * The sequence has size() == additionalItems.
//     * */
//    public static List<String> generateIdSequence(int additionalItems){
//        List<String> sequence = new ArrayList<String>(additionalItems);
//        for(int i = 1; i <= additionalItems; i++){
//            sequence.add(String.valueOf(i));
//        }
//        return sequence;
//    }
//    
//    /**
//     * Creates a sequence with start value currentMaxId+1.<br>
//     * The last element has id == currentMaxId+additionalItems.<br>
//     * The sequence has size() == additionalItems.<br>
//     * The new maxId for the container can be set >= the last id in the sequence.
//     * */
//    public static final List<String> generateIdSequence(String currentMaxId, int additionalItems){
//
//        List<String> sequence = new ArrayList<String>(additionalItems);
//        BigInteger a = new BigInteger(currentMaxId);
//        BigInteger b;
//
//        for(int i = 1; i <= additionalItems; i++){
//            b = new BigInteger(String.valueOf(i));
//            sequence.add(a.add(b).toString());
//        }
//
//        return sequence;
//    }

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
