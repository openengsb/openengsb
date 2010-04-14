
package org.openengsb.core.xmlmapping;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:element xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLReturnValue">
 *   &lt;xs:complexType>
 *     &lt;xs:sequence>
 *       &lt;xs:element type="XMLTypedValue" name="value"/>
 *     &lt;/xs:sequence>
 *   &lt;/xs:complexType>
 * &lt;/xs:element>
 * </pre>
 */
public class XMLReturnValue
{
    private XMLTypedValue value;

    /** 
     * Get the 'value' element value.
     * 
     * @return value
     */
    public XMLTypedValue getValue() {
        return value;
    }

    /** 
     * Set the 'value' element value.
     * 
     * @param value
     */
    public void setValue(XMLTypedValue value) {
        this.value = value;
    }
}
