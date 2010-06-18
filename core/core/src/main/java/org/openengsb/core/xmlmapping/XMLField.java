
package org.openengsb.core.xmlmapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "fieldName", "value" })
public class XMLField
{
    private String fieldName;
    private XMLMappable value;

    @XmlElement(required = true)
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @XmlElement(required = true)
    public XMLMappable getValue() {
        return value;
    }

    public void setValue(XMLMappable value) {
        this.value = value;
    }
}
