package org.openengsb.framework.vfs.configurationserviceapi.common;

import java.nio.file.Path;
import java.util.Date;

public interface Tag extends Comparable<Tag> {
    /**
     * Returns the name of the tag.
     * @return The name of the tag.
     */
    String getName();

    /**
     * Returns the path to the tag.
     * @return The path to the tag.
     */
    Path getPath();

    /**
     * Returns the date of the tag.
     * @return The date of the tag.
     */
    Date getDate();
}
