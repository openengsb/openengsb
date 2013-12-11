package org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.openengsb.framework.vfs.api.common.Tag;

public interface FileOperator {

    /**
     * Returns a list of tags from the given path.
     *
     * @param tagspath
     * @return a list of tags from the given path.
     */
    List<Tag> getTagsFromDirectory(Path tagsPath);

    void createDirectories(Path path) throws IOException;

    void copy(Path source, Path destination) throws IOException;
}
