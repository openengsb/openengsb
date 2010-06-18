
package org.openengsb.core.xmlmapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class XMLReference
{
    private String id;

    @XmlElement(required = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
