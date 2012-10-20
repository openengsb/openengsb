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

import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;

/**
 * Utility class offering common operations on {@link Entry} and {@link Dn} objects.
 * */
public final class LdapUtils {

    private LdapUtils() {
    }

    /**
     * Returns a {@link Dn} consisting of baseDn extended by an Rdn of type rdnAttribute and value rdnValue.
     * */
    public static Dn concatDn(String rdnAttribute, String rdnValue, Dn basedn) {
        try {
            Rdn rdn = new Rdn(rdnAttribute, rdnValue);
            return basedn.add(rdn);
        } catch (LdapInvalidDnException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * Returns the value of attributeType from entry.
     * */
    public static String extractAttributeNoEmptyCheck(Entry entry, String attributeTye) {
        //TODO: Nullpointerex ???!!!
        //merge this with extractfirstValueOfAttribute!!!
        Attribute attribute = entry.get(attributeTye);
        if (attribute == null) {
            return null;
        }
        try {
            return attribute.getString();
        } catch (LdapInvalidAttributeValueException e) {
            throw new LdapRuntimeException(e);
        }
    }

    // TODO make more general method where it is optional if empty flag should
    // be checked or not. so far this method always checks it although it may
    // also be used for attributes where empty flag is not allowed.
    /**
     * Returns the value of the first occurence of attributeType from entry.
     * */
    public static String extractFirstValueOfAttribute(Entry entry, String attributeTye) {

        if (entry == null) {
            return null;
        }

        Attribute attribute = entry.get(attributeTye);
        Attribute emptyFlagAttribute = entry.get(SchemaConstants.emptyFlagAttribute);

        boolean empty = false;
        try {
            if (attribute != null) {
                return attribute.getString();
            } else if (emptyFlagAttribute != null) {
                empty = Boolean.valueOf(emptyFlagAttribute.getString());
            }
        } catch (LdapInvalidAttributeValueException e) {
            throw new ObjectClassViolationException(e);
        }
        return empty ? new String() : null;
    }

    /**
     * Returns the value of the first occurence of attributeType from each entry.
     * */
    public static List<String> extractFirstValueOfAttribute(List<Entry> entries, String attributeType)
        throws ObjectClassViolationException {
        List<String> result = new LinkedList<String>();
        for (Entry e : entries) {
            result.add(extractFirstValueOfAttribute(e, attributeType));
        }
        return result;
    }

    /**
     * Returns the value of the first occurence of attributeType from each entry in the cursor.
     * */
    public static List<String> extractFirstValueOfAttribute(SearchCursor cursor, String attributeType) {
        List<String> result = new LinkedList<String>();
        try {
            while (cursor.next()) {
                Entry entry = cursor.getEntry();
                result.add(extractFirstValueOfAttribute(entry, attributeType));
            }
        } catch (Exception e) {
            throw new LdapRuntimeException(e);
        }
        return result;
    }

}
