package org.openengsb.core.api.security.model;

import java.util.Map;

public interface Permission {

    Map<String, String> toAttributes();

}
