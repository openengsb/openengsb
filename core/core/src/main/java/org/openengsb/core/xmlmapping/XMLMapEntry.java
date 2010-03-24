
package org.openengsb.core.xmlmapping;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLMapEntry">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="xs:string" name="key"/>
 *     &lt;xs:element type="XMLMapable" name="value"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLMapEntry
{
    private String key;
    private XMLMapable value;

    /** 
     * Get the 'key' element value.
     * 
     * @return value
     */
    public String getKey() {
        return key;
    }

    /** 
     * Set the 'key' element value.
     * 
     * @param key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /** 
     * Get the 'value' element value.
     * 
     * @return value
     */
    public XMLMapable getValue() {
        return value;
    }

    /** 
     * Set the 'value' element value.
     * 
     * @param value
     */
    public void setValue(XMLMapable value) {
        this.value = value;
    }
}
