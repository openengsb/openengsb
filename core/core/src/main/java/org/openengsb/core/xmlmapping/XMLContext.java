
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType
public class XMLContext
{
    private List<XMLContextEntry> entryList = new ArrayList<XMLContextEntry>();

    @XmlElement(required = true)
    public List<XMLContextEntry> getEntries() {
        return entryList;
    }

    public void setEntries(List<XMLContextEntry> list) {
        entryList = list;
    }
}
