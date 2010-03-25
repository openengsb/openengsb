
package org.openengsb.core.xmlmapping;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLReference">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="xs:int" name="id"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLReference
{
    private int id;

    /** 
     * Get the 'id' element value.
     * 
     * @return value
     */
    public int getId() {
        return id;
    }

    /** 
     * Set the 'id' element value.
     * 
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }
}
