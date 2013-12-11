package org.openengsb.framework.vfs.api.repositoryhandler;

import java.nio.file.Path;
import org.openengsb.framework.vfs.api.common.Tag;

/**
 * The RepositoryHandler is responsible for handling the configuration repository that is used by VFS to configure openengsb.
 */
public interface RepositoryHandler {
    /**
     * Creates a tag from the specified path.
     * @param path The specified path.
     * @param tagName The name of the tag.
     */
    void tagDirectory(Path path, String tagName);

    /**
     * Gets the youngest tag that is older than the current tag.
     * @param currentTag The current tag.
     * @return The youngest tag that is older than the current tag or null if no older tag can be found.
     */
    Tag getPreviousTag(Tag currentTag);

    Path getRepositoryPath();
    Path getConfigurationPath();
}
