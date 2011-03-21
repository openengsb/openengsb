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

package org.openengsb.domain.contact;

import java.util.ArrayList;
import java.util.Date;

import org.openengsb.core.common.Domain;
import org.openengsb.domain.contact.models.Contact;
import org.openengsb.domain.contact.models.Location;

/**
 * This domain is used to maintain different contact books in different tools like gcontacts or facebook.
 */
public interface ContactDomain extends Domain {

    /**
     * creates a contact on the server and returns the generated id
     */
    String createContact(Contact contact);
    
    /**
     * updates a contact on the server
     */
    void updateContact(Contact contact);
    
    /**
     * deletes a contact on the server
     */
    void deleteContact(String id);
    
    /**
     * retrieves a list of contacts from the server based on "query by example" 
     */
    ArrayList<Contact> retrieveContacts(String id, String name, String homepage, 
        Location location, Date date, String comment);
}
