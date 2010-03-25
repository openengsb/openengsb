
package org.openengsb.core.xmlmapping;

import java.util.ArrayList;
import java.util.List;

/** 
 * Schema fragment(s) for this class:
 * <pre>
 * &lt;xs:complexType xmlns:xs="http://www.w3.org/2001/XMLSchema" name="XMLMapable">
 *   &lt;!-- Reference to class org.openengsb.core.xmlmapping.XMLMapable -->
 * &lt;/xs:complexType>
 * </pre>
 */
public class XMLMapable
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
    private List<XMLMapable> listList = new ArrayList<XMLMapable>();
    private XMLEvent event;
    private List<XMLMapEntry> mapList = new ArrayList<XMLMapEntry>();
    private XMLContext context;
    private int id;
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
     * Check if Lists is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifList() {
        return choiceSelect == LIST_CHOICE;
    }

    /** 
     * Get the list of 'list' element items.
     * 
     * @return list
     */
    public List<XMLMapable> getLists() {
        return listList;
    }

    /** 
     * Set the list of 'list' element items.
     * 
     * @param list
     */
    public void setLists(List<XMLMapable> list) {
        setChoiceSelect(LIST_CHOICE);
        listList = list;
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
     * Check if Maps is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifMap() {
        return choiceSelect == MAP_CHOICE;
    }

    /** 
     * Get the list of 'map' element items.
     * 
     * @return list
     */
    public List<XMLMapEntry> getMaps() {
        return mapList;
    }

    /** 
     * Set the list of 'map' element items.
     * 
     * @param list
     */
    public void setMaps(List<XMLMapEntry> list) {
        setChoiceSelect(MAP_CHOICE);
        mapList = list;
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
