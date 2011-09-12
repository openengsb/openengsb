package org.openengsb.core.security.model;

import java.util.Map;

import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.util.BeanUtils2;

public class GenericPermission implements Permission {

    @Override
    public Map<String, String> toAttributes() {
        return BeanUtils2.buildAttributeMap(this);
    }
    
    

}
