package org.openengsb.framework.vfs.api.common;

import java.nio.file.Path;
import java.util.Date;

/**
 * A Tag is used to describe a state of the configuration that can be used to configure openengsb.
 */
public interface Tag extends Comparable<Tag> {
    String getName();
    Path getTagPath();
    Date getCreationDate();
}
