package org.openengsb.core.api.model;

import java.util.List;

/**
 * Represents a generic model which should be used by any model data in the domains. With this model it is possible
 * to use one model for all kinds of domain model data. The function here defined should convert any specific domain
 * tool data into our most generic model type.
 */
public interface OpenEngSBModel {

    /**
     * Returns a list of OpenEngSBModelEntries. The list contains all data fields which are used by the specific
     * domain.
     */
    List<OpenEngSBModelEntry> getOpenEngSBModelEntries();
}
