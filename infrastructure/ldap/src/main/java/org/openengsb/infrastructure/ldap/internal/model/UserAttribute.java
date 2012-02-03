package org.openengsb.infrastructure.ldap.internal.model;

import java.util.List;
import org.apache.directory.shared.ldap.model.entry.Entry;

public class UserAttribute {
    
    private String username;
    private String attributeName;
    private Object[] values;
    
    public UserAttribute(String username, String attributeName, Object[] values){
        
    }

    public List<Entry> getStructure(){
        return null;
    }
    
}
