
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLMapEntryList">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="XMLMapEntry" name="mapEntry" maxOccurs="unbounded"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLMapEntryList
{
    private List<XMLMapEntry> mapEntryList = new ArrayList<XMLMapEntry>();

    /** 
     * Get the list of 'mapEntry' element items.
     * 
     * @return list
     */
    public List<XMLMapEntry> getMapEntries() {
        return mapEntryList;
    }

    /** 
     * Set the list of 'mapEntry' element items.
     * 
     * @param list
     */
    public void setMapEntries(List<XMLMapEntry> list) {
        mapEntryList = list;
    }
}
