/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.issue.trac.internal.models.xmlrpc;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;

/**
 * This class is copied and slightly modified from the Trac XML-RPC Plugin Java example
 * (http://trac-hacks.org/wiki/XmlRpcPlugin#UsingfromJava). See NOTICE for further details
 */
public interface Ticket {
    public interface TicketProperty {
        Vector<?> getAll();

        Hashtable<?, ?> get(String name);
    }

    public interface Milestone extends TicketProperty {
    }

    public interface Severity extends TicketProperty {
    }

    public interface Type extends TicketProperty {
    }

    public interface Resoluton extends TicketProperty {
    }

    public interface Priority extends TicketProperty {
    }

    public interface Component extends TicketProperty {
    }

    public interface Version extends TicketProperty {
    }

    public interface Status extends TicketProperty {
    }

    Vector<?> query(); // qstr="status!=closed"

    Vector<?> query(String qstr);

    Integer delete(Integer id) throws XmlRpcException;

    Integer create(String summary, String description) throws XmlRpcException;

    Integer create(String summary, String description, Hashtable<?, ?> attribute) throws XmlRpcException;

    Integer create(String summary, String description, Hashtable<?, ?> attribute, Boolean notify)
        throws XmlRpcException;

    Vector<?> get(Integer id) throws XmlRpcException;

    Vector<?> update(Integer id, String comment) throws XmlRpcException;

    Vector<?> update(Integer id, String comment, Hashtable<?, ?> attributes) throws XmlRpcException;

    Vector<?> update(Integer id, String comment, Hashtable<?, ?> attributes, Boolean notify) throws XmlRpcException;

    Hashtable<?, ?> changeLog(Integer id);

    Hashtable<?, ?> changeLog(Integer id, Integer when);

    Vector<?> listAttachments(Integer ticket);

    byte[] getAttachment(Integer ticket, String filename);

    String putAttachment(Integer ticket, String filename, String description, byte[] data);

    String putAttachment(Integer ticket, String filename, String description, byte[] data, Boolean replace);

    Boolean deleteAttachment(Integer ticket, String filename);

    Vector<HashMap<?, ?>> getTicketFields();
}
