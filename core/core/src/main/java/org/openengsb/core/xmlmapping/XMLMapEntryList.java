
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
public class XMLMapEntryList
{
    private List<XMLMapEntry> mapEntryList = new ArrayList<XMLMapEntry>();

    @XmlElement(required = true)
    public List<XMLMapEntry> getMapEntries() {
        return mapEntryList;
    }

    public void setMapEntries(List<XMLMapEntry> list) {
        mapEntryList = list;
    }
}
