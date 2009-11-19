/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

package org.openengsb.core.messaging;

import java.io.StringReader;
import java.io.StringWriter;

import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.SerializationException;

public abstract class Segment {
    private final String name;
    private final String format;
    private final String domainConcept;

    private final static JibxXmlSerializer serializer = new JibxXmlSerializer();

    protected Segment(String name, String format, String domainConcept) {
        this.name = name;
        this.format = format;
        this.domainConcept = domainConcept;
        validate();
    }

    public void validate() {
        if (name == null) {
            throw new IllegalStateException("Name must not be null");
        }
        if (format == null) {
            throw new IllegalStateException("Format must not be null");
        }
        if (domainConcept == null) {
            throw new IllegalStateException("DomainConcept must not be null");
        }
    }

    public String toXML() throws SerializationException {
        StringWriter writer = new StringWriter();
        serializer.serialize(this, writer);
        return writer.toString();
    }

    public static Segment fromXML(String xml) throws SerializationException {
        return serializer.deserialize(Segment.class, new StringReader(xml));
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public String getDomainConcept() {
        return domainConcept;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + name.hashCode();
        hash = hash * 31 + format.hashCode();
        hash = hash * 31 + domainConcept.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Segment)) {
            return false;
        }

        Segment other = (Segment) obj;

        return name.equals(other.name) && format.equals(other.format) && domainConcept.equals(other.domainConcept);
    }
}
