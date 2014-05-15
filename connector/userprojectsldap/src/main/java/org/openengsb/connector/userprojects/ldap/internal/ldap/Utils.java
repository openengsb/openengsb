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

package org.openengsb.connector.userprojects.ldap.internal.ldap;

import java.util.LinkedList;
import java.util.List;

import org.apache.directory.shared.ldap.model.cursor.SearchCursor;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.openengsb.connector.userprojects.ldap.internal.LdapRuntimeException;

/**
 * Utility class offering common operations on {@link Entry} and {@link Dn} objects.
 * */
public final class Utils {

    private Utils() {
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
     * Returns the value of the attribute of attributeType from entry.
     * */
    public static String extractAttributeValueNoEmptyCheck(Entry entry, String attributeType) {
        Attribute attribute = entry.get(attributeType);
        if (attribute == null) {
            return null;
        }
        try {
            return attribute.getString();
        } catch (LdapInvalidAttributeValueException e) {
            throw new LdapRuntimeException(e);
        }
    }

    /**
     * Returns the value of the first occurrence of attributeType from entry.
     * */
    public static String extractAttributeEmptyCheck(Entry entry, String attributeType) {
        Attribute attribute = entry.get(attributeType);
        Attribute emptyFlagAttribute = entry.get(SchemaConstants.EMPTY_FLAG_ATTRIBUTE);
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
     * Returns the value of the first occurrence of attributeType from each entry.
     * */
    public static List<String> extractAttributeEmptyCheck(List<Entry> entries, String attributeType) {
        List<String> result = new LinkedList<String>();
        for (Entry e : entries) {
            result.add(extractAttributeEmptyCheck(e, attributeType));
        }
        return result;
    }

    /**
     * Returns the value of the first occurrence of attributeType from each entry in the cursor.
     * */
    public static List<String> extractAttributeEmptyCheck(SearchCursor cursor, String attributeType) {
        List<String> result = new LinkedList<String>();
        try {
            while (cursor.next()) {
                Entry entry = cursor.getEntry();
                result.add(extractAttributeEmptyCheck(entry, attributeType));
            }
        } catch (Exception e) {
            throw new LdapRuntimeException(e);
        }
        return result;
    }

}
