
package org.openengsb.core.xmlmapping;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLMapEntry">
 *   &lt;xs:sequence>
 *     &lt;xs:element type="XMLMappable" name="key"/>
 *     &lt;xs:element type="XMLMappable" name="value"/>
 *   &lt;/xs:sequence>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLMapEntry
{
    private XMLMappable key;
    private XMLMappable value;

    /** 
     * Get the 'key' element value.
     * 
     * @return value
     */
    public XMLMappable getKey() {
        return key;
    }

    /** 
     * Set the 'key' element value.
     * 
     * @param key
     */
    public void setKey(XMLMappable key) {
        this.key = key;
    }

    /** 
     * Get the 'value' element value.
     * 
     * @return value
     */
    public XMLMappable getValue() {
        return value;
    }

    /** 
     * Set the 'value' element value.
     * 
     * @param value
     */
    public void setValue(XMLMappable value) {
        this.value = value;
    }
}
