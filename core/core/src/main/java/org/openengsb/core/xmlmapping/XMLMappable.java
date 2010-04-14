
package org.openengsb.core.xmlmapping;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLMappable">
 *   &lt;xs:sequence>
 *     &lt;xs:choice>
 *       &lt;xs:element type="xs:string" name="null"/>
 *       &lt;xs:element type="XMLReference" name="reference"/>
 *       &lt;xs:element type="XMLPrimitive" name="primitive"/>
 *       &lt;xs:element type="XMLBean" name="bean"/>
 *       &lt;xs:element type="XMLMappableList" name="list"/>
 *       &lt;xs:element type="XMLEvent" name="event"/>
 *       &lt;xs:element type="XMLMapEntryList" name="map"/>
 *       &lt;xs:element type="XMLContext" name="context"/>
 *     &lt;/xs:choice>
 *     &lt;xs:element type="xs:string" nillable="true" name="id"/>
 *   &lt;/xs:sequence>
 *   &lt;xs:attribute type="xs:string" name="domainConcept"/>
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLMappable
{
    private int choiceSelect = -1;
    private static final int NULL_CHOICE = 0;
    private static final int REFERENCE_CHOICE = 1;
    private static final int PRIMITIVE_CHOICE = 2;
    private static final int BEAN_CHOICE = 3;
    private static final int LIST_CHOICE = 4;
    private static final int EVENT_CHOICE = 5;
    private static final int MAP_CHOICE = 6;
    private static final int CONTEXT_CHOICE = 7;
    private String _null;
    private XMLReference reference;
    private XMLPrimitive primitive;
    private XMLBean bean;
    private XMLMappableList list;
    private XMLEvent event;
    private XMLMapEntryList map;
    private XMLContext context;
    private String id;
    private String domainConcept;

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
     * Check if Null is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifNull() {
        return choiceSelect == NULL_CHOICE;
    }

    /** 
     * Get the 'null' element value.
     * 
     * @return value
     */
    public String getNull() {
        return _null;
    }

    /** 
     * Set the 'null' element value.
     * 
     * @param _null
     */
    public void setNull(String _null) {
        setChoiceSelect(NULL_CHOICE);
        this._null = _null;
    }

    /** 
     * Check if Reference is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifReference() {
        return choiceSelect == REFERENCE_CHOICE;
    }

    /** 
     * Get the 'reference' element value.
     * 
     * @return value
     */
    public XMLReference getReference() {
        return reference;
    }

    /** 
     * Set the 'reference' element value.
     * 
     * @param reference
     */
    public void setReference(XMLReference reference) {
        setChoiceSelect(REFERENCE_CHOICE);
        this.reference = reference;
    }

    /** 
     * Check if Primitive is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifPrimitive() {
        return choiceSelect == PRIMITIVE_CHOICE;
    }

    /** 
     * Get the 'primitive' element value.
     * 
     * @return value
     */
    public XMLPrimitive getPrimitive() {
        return primitive;
    }

    /** 
     * Set the 'primitive' element value.
     * 
     * @param primitive
     */
    public void setPrimitive(XMLPrimitive primitive) {
        setChoiceSelect(PRIMITIVE_CHOICE);
        this.primitive = primitive;
    }

    /** 
     * Check if Bean is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifBean() {
        return choiceSelect == BEAN_CHOICE;
    }

    /** 
     * Get the 'bean' element value.
     * 
     * @return value
     */
    public XMLBean getBean() {
        return bean;
    }

    /** 
     * Set the 'bean' element value.
     * 
     * @param bean
     */
    public void setBean(XMLBean bean) {
        setChoiceSelect(BEAN_CHOICE);
        this.bean = bean;
    }

    /** 
     * Check if List is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifList() {
        return choiceSelect == LIST_CHOICE;
    }

    /** 
     * Get the 'list' element value.
     * 
     * @return value
     */
    public XMLMappableList getList() {
        return list;
    }

    /** 
     * Set the 'list' element value.
     * 
     * @param list
     */
    public void setList(XMLMappableList list) {
        setChoiceSelect(LIST_CHOICE);
        this.list = list;
    }

    /** 
     * Check if Event is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifEvent() {
        return choiceSelect == EVENT_CHOICE;
    }

    /** 
     * Get the 'event' element value.
     * 
     * @return value
     */
    public XMLEvent getEvent() {
        return event;
    }

    /** 
     * Set the 'event' element value.
     * 
     * @param event
     */
    public void setEvent(XMLEvent event) {
        setChoiceSelect(EVENT_CHOICE);
        this.event = event;
    }

    /** 
     * Check if Map is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifMap() {
        return choiceSelect == MAP_CHOICE;
    }

    /** 
     * Get the 'map' element value.
     * 
     * @return value
     */
    public XMLMapEntryList getMap() {
        return map;
    }

    /** 
     * Set the 'map' element value.
     * 
     * @param map
     */
    public void setMap(XMLMapEntryList map) {
        setChoiceSelect(MAP_CHOICE);
        this.map = map;
    }

    /** 
     * Check if Context is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifContext() {
        return choiceSelect == CONTEXT_CHOICE;
    }

    /** 
     * Get the 'context' element value.
     * 
     * @return value
     */
    public XMLContext getContext() {
        return context;
    }

    /** 
     * Set the 'context' element value.
     * 
     * @param context
     */
    public void setContext(XMLContext context) {
        setChoiceSelect(CONTEXT_CHOICE);
        this.context = context;
    }

    /** 
     * Get the 'id' element value.
     * 
     * @return value
     */
    public String getId() {
        return id;
    }

    /** 
     * Set the 'id' element value.
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /** 
     * Get the 'domainConcept' attribute value.
     * 
     * @return value
     */
    public String getDomainConcept() {
        return domainConcept;
    }

    /** 
     * Set the 'domainConcept' attribute value.
     * 
     * @param domainConcept
     */
    public void setDomainConcept(String domainConcept) {
        this.domainConcept = domainConcept;
    }
}
