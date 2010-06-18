
package org.openengsb.core.xmlmapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "type", "value" })
public class XMLTypedValue
{
    private String type;
    private XMLMappable value;

    @XmlElement(required = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement(required = true)
    public XMLMappable getValue() {
        return value;
    }

    public void setValue(XMLMappable value) {
        this.value = value;
    }
}
