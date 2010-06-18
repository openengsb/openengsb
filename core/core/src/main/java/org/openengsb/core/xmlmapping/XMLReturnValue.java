
package org.openengsb.core.xmlmapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class XMLReturnValue
{
    private XMLTypedValue value;

    @XmlElement(required = true)
    public XMLTypedValue getValue() {
        return value;
    }

    public void setValue(XMLTypedValue value) {
        this.value = value;
    }
}
