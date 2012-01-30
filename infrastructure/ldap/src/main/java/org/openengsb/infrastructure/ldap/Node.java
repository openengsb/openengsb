package org.openengsb.infrastructure.ldap;

import java.util.Arrays;
import java.util.List;

public class Node {

    //private Node[] children;

    private Node parent;
    private List<Attribute> attributes;
    private List<String> objectClasses;
    
    public Node() {
    }
    
    /** The list must not be empty. The first attribute becomes the Rdn.*/
    public Node(List<Attribute> attributes) {
        if(attributes == null || attributes.isEmpty()){
            throw new IllegalArgumentException("List is empty or null.");
        }
        this.attributes = attributes;
    }
    
    public Node(Node parent){
        this.parent = parent;
    }
    
    public Node(Node parent, String rdnType, String rdnValue){
        this.parent = parent;
        attributes = Arrays.asList(new Attribute(rdnType, rdnValue));
    }
    
    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<String> getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(List<String> objectClasses) {
        this.objectClasses = objectClasses;
    }

    public Attribute getRdn() {
        return attributes.get(0);
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }
    
    /** The list must not be empty. The first attribute becomes the Rdn.*/
    public void setAttributes(List<Attribute> attributes) {
        if(attributes == null || attributes.isEmpty()){
            throw new IllegalArgumentException("List is empty or null.");
        }
        this.attributes = attributes;
    }

    public List<String> getUpRdns(Node node, List<String> upRdns){
        Attribute rdn = getRdn();
        upRdns.add(0, rdn.getValue());
        upRdns.add(0, rdn.getType());
        return parent == null ? upRdns : getUpRdns(parent, upRdns); 
    }

}
