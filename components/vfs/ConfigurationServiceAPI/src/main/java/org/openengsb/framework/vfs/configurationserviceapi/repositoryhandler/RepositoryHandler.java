package org.openengsb.framework.vfs.configurationserviceapi.repositoryhandler;

import java.nio.file.Path;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;

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

    /**
     * Returns the path of the repository directory.
     * @return the path of the repository directory.
     */
    Path getRepositoryPath();


    /**
     * Returns the path of the configuration directory.
     * @return the path of the configuration directory.
     */
    Path getConfigurationPath();
}
