package org.openengsb.core.edbi.jdbc.api;

import org.openengsb.core.edbi.api.EDBIndexException;

/**
 * Indicates that a Table does not exists, where one was expected.
 */
public class NoSuchTableException extends EDBIndexException {

    public NoSuchTableException(String message) {
        super(message);
    }
}
