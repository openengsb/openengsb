package org.openengsb.core.api.security;

import java.util.Collection;

import org.openengsb.core.api.security.model.SecurityAttributeEntry;

public interface SecurityAttributeProvider {

    Collection<SecurityAttributeEntry> getAttribute(Object o);

}
