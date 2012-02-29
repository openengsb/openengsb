package org.openengsb.infrastructure.ldap.util;

import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.openengsb.infrastructure.ldap.internal.ObjectClassViolationException;
import org.openengsb.separateProject.SchemaConstants;

public class LdapUtils {

    public static Dn concatDn(String rdnAttribute, String rdnValue, Dn basedn){
        try {
            Rdn rdn = new Rdn(rdnAttribute, rdnValue);
            return basedn.add(rdn);
        } catch (LdapInvalidDnException e) {
            throw new RuntimeException(e);
        }
    }

    public static String extractAttributeNoEmptyCheck(Entry entry, String attributeTye){
        Attribute attribute = entry.get(attributeTye);
        if(attribute == null){
            return null;
        }
        try {
            return attribute.getString();
        } catch (LdapInvalidAttributeValueException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO make more general method where it is optional if empty flag should be checked or not.
    //so far this method always checks it although it may also be used for attributes where empty flag is not allowed.
    public static String extractFirstValueOfAttribute(Entry entry, String attributeTye){

        if(entry == null){
            return null;
        }
        
        Attribute attribute = entry.get(attributeTye);
        Attribute emptyFlagAttribute = entry.get(SchemaConstants.emptyFlagAttribute);

        boolean empty = false;
        try {
            if(attribute != null){
                return attribute.getString();
            } else if(emptyFlagAttribute != null){
                empty = Boolean.valueOf(emptyFlagAttribute.getString());
            }
        } catch (LdapInvalidAttributeValueException e) {
            throw new ObjectClassViolationException(e);
        }
        return empty ? new String() : null;
    }

    public static List<String> extractFirstValueOfAttribute(SearchCursor cursor, String attributeType){
        List<String> result = new LinkedList<String>();
        try {
            while(cursor.next()){
                Entry entry = cursor.getEntry();
                result.add(extractFirstValueOfAttribute(entry, attributeType));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
