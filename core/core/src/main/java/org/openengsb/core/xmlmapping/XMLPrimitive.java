
package org.openengsb.core.xmlmapping;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLPrimitive">
 *   &lt;xs:choice>
 *     &lt;xs:element type="xs:string" nillable="true" name="string"/>
 *     &lt;xs:element type="xs:float" nillable="true" name="float"/>
 *     &lt;xs:element type="xs:double" nillable="true" name="double"/>
 *     &lt;xs:element type="xs:boolean" nillable="true" name="boolean"/>
 *     &lt;xs:element type="xs:int" nillable="true" name="int"/>
 *     &lt;xs:element type="xs:long" nillable="true" name="long"/>
 *     &lt;xs:element type="xs:short" nillable="true" name="short"/>
 *     &lt;xs:element type="xs:byte" nillable="true" name="byte"/>
 *     &lt;xs:element type="xs:base64Binary" nillable="true" name="base64Binary"/>
 *   &lt;/xs:choice>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLPrimitive
{
    private int choiceSelect = -1;
    private static final int STRING_CHOICE = 0;
    private static final int FLOAT_CHOICE = 1;
    private static final int DOUBLE_CHOICE = 2;
    private static final int BOOLEAN_CHOICE = 3;
    private static final int INT_CHOICE = 4;
    private static final int LONG_CHOICE = 5;
    private static final int SHORT_CHOICE = 6;
    private static final int BYTE_CHOICE = 7;
    private static final int BASE64_BINARY_CHOICE = 8;
    private String string;
    private Float _float;
    private Double _double;
    private Boolean _boolean;
    private Integer _int;
    private Long _long;
    private Short _short;
    private Byte _byte;
    private byte[] base64Binary;

    private void setChoiceSelect(int choice) {
        if (choiceSelect == -1) {
            choiceSelect = choice;
        } else if (choiceSelect != choice) {
            throw new IllegalStateException(
                    "Need to call clearChoiceSelect() before changing existing choice");
        }
    }

    /** 
     * Clear the choice selection.
     */
    public void clearChoiceSelect() {
        choiceSelect = -1;
    }

    /** 
     * Check if String is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifString() {
        return choiceSelect == STRING_CHOICE;
    }

    /** 
     * Get the 'string' element value.
     * 
     * @return value
     */
    public String getString() {
        return string;
    }

    /** 
     * Set the 'string' element value.
     * 
     * @param string
     */
    public void setString(String string) {
        setChoiceSelect(STRING_CHOICE);
        this.string = string;
    }

    /** 
     * Check if Float is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifFloat() {
        return choiceSelect == FLOAT_CHOICE;
    }

    /** 
     * Get the 'float' element value.
     * 
     * @return value
     */
    public Float getFloat() {
        return _float;
    }

    /** 
     * Set the 'float' element value.
     * 
     * @param _float
     */
    public void setFloat(Float _float) {
        setChoiceSelect(FLOAT_CHOICE);
        this._float = _float;
    }

    /** 
     * Check if Double is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifDouble() {
        return choiceSelect == DOUBLE_CHOICE;
    }

    /** 
     * Get the 'double' element value.
     * 
     * @return value
     */
    public Double getDouble() {
        return _double;
    }

    /** 
     * Set the 'double' element value.
     * 
     * @param _double
     */
    public void setDouble(Double _double) {
        setChoiceSelect(DOUBLE_CHOICE);
        this._double = _double;
    }

    /** 
     * Check if Boolean is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifBoolean() {
        return choiceSelect == BOOLEAN_CHOICE;
    }

    /** 
     * Get the 'boolean' element value.
     * 
     * @return value
     */
    public Boolean getBoolean() {
        return _boolean;
    }

    /** 
     * Set the 'boolean' element value.
     * 
     * @param _boolean
     */
    public void setBoolean(Boolean _boolean) {
        setChoiceSelect(BOOLEAN_CHOICE);
        this._boolean = _boolean;
    }

    /** 
     * Check if Int is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifInt() {
        return choiceSelect == INT_CHOICE;
    }

    /** 
     * Get the 'int' element value.
     * 
     * @return value
     */
    public Integer getInt() {
        return _int;
    }

    /** 
     * Set the 'int' element value.
     * 
     * @param _int
     */
    public void setInt(Integer _int) {
        setChoiceSelect(INT_CHOICE);
        this._int = _int;
    }

    /** 
     * Check if Long is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifLong() {
        return choiceSelect == LONG_CHOICE;
    }

    /** 
     * Get the 'long' element value.
     * 
     * @return value
     */
    public Long getLong() {
        return _long;
    }

    /** 
     * Set the 'long' element value.
     * 
     * @param _long
     */
    public void setLong(Long _long) {
        setChoiceSelect(LONG_CHOICE);
        this._long = _long;
    }

    /** 
     * Check if Short is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifShort() {
        return choiceSelect == SHORT_CHOICE;
    }

    /** 
     * Get the 'short' element value.
     * 
     * @return value
     */
    public Short getShort() {
        return _short;
    }

    /** 
     * Set the 'short' element value.
     * 
     * @param _short
     */
    public void setShort(Short _short) {
        setChoiceSelect(SHORT_CHOICE);
        this._short = _short;
    }

    /** 
     * Check if Byte is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifByte() {
        return choiceSelect == BYTE_CHOICE;
    }

    /** 
     * Get the 'byte' element value.
     * 
     * @return value
     */
    public Byte getByte() {
        return _byte;
    }

    /** 
     * Set the 'byte' element value.
     * 
     * @param _byte
     */
    public void setByte(Byte _byte) {
        setChoiceSelect(BYTE_CHOICE);
        this._byte = _byte;
    }

    /** 
     * Check if Base64Binary is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifBase64Binary() {
        return choiceSelect == BASE64_BINARY_CHOICE;
    }

    /** 
     * Get the 'base64Binary' element value.
     * 
     * @return value
     */
    public byte[] getBase64Binary() {
        return base64Binary;
    }

    /** 
     * Set the 'base64Binary' element value.
     * 
     * @param base64Binary
     */
    public void setBase64Binary(byte[] base64Binary) {
        setChoiceSelect(BASE64_BINARY_CHOICE);
        this.base64Binary = base64Binary;
    }
}
