package org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations;

import java.nio.file.Path;
import java.util.List;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;

public interface FileOperator {

    /**
     * Returns a list of tags from the given path.
     *
     * @param tagspath
     * @return a list of tags from the given path.
     */
    List<Tag> getTagsFromDirectory(Path tagsPath);
}
