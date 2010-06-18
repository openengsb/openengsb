
package org.openengsb.core.xmlmapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "key", "value" })
public class XMLMapEntry
{
    private XMLMappable key;
    private XMLMappable value;

    @XmlElement(required = true)
    public XMLMappable getKey() {
        return key;
    }

    public void setKey(XMLMappable key) {
        this.key = key;
    }

    @XmlElement(required = true)
    public XMLMappable getValue() {
        return value;
    }

    public void setValue(XMLMappable value) {
        this.value = value;
    }
}
