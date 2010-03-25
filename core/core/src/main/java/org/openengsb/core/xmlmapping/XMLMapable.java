
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
    private static final int VALUE_CHOICE = 0;
    private static final int ARRAY_CHOICE = 1;
    private static final int EVENT_CHOICE = 2;
    private static final int MAP_CHOICE = 3;
    private static final int CONTEXT_CHOICE = 4;
    private XMLPrimitive value;
    private List<XMLMapable> arrayList = new ArrayList<XMLMapable>();
    private XMLEvent event;
    private List<XMLMapEntry> mapList = new ArrayList<XMLMapEntry>();
    private XMLContext context;
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
     * Check if Value is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifValue() {
        return choiceSelect == VALUE_CHOICE;
    }

    /** 
     * Get the 'value' element value.
     * 
     * @return value
     */
    public XMLPrimitive getValue() {
        return value;
    }

    /** 
     * Set the 'value' element value.
     * 
     * @param value
     */
    public void setValue(XMLPrimitive value) {
        setChoiceSelect(VALUE_CHOICE);
        this.value = value;
    }

    /** 
     * Check if Arraies is current selection for choice.
     * 
     * @return <code>true</code> if selection, <code>false</code> if not
     */
    public boolean ifArray() {
        return choiceSelect == ARRAY_CHOICE;
    }

    /** 
     * Get the list of 'array' element items.
     * 
     * @return list
     */
    public List<XMLMapable> getArraies() {
        return arrayList;
    }

    /** 
     * Set the list of 'array' element items.
     * 
     * @param list
     */
    public void setArraies(List<XMLMapable> list) {
        setChoiceSelect(ARRAY_CHOICE);
        arrayList = list;
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
