package org.openengsb.ui.common.model;

import org.openengsb.core.security.model.GenericPermission;

public class UiPermission extends GenericPermission {
    private String securityAttribute;

    public UiPermission() {
    }

    public UiPermission(String componentSecurityContext) {
        this.securityAttribute = componentSecurityContext;
    }

    public String getSecurityAttribute() {
        return securityAttribute;
    }

    public void setSecurityAttribute(String securityAttribute) {
        this.securityAttribute = securityAttribute;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((securityAttribute == null) ? 0 : securityAttribute.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UiPermission other = (UiPermission) obj;
        if (securityAttribute == null) {
            if (other.securityAttribute != null)
                return false;
        } else if (!securityAttribute.equals(other.securityAttribute))
            return false;
        return true;
    }

}
