package org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;
import org.openengsb.framework.vfs.vfsrepositoryhandler.tags.ConfigurationTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VFSFileOperator implements FileOperator {

    private final Logger logger = LoggerFactory.getLogger(VFSFileOperator.class);

    public List<Tag> getTagsFromDirectory(Path tagsPath) {
        List<Tag> tags = new ArrayList<Tag>();
        //TODO handling of files that are not tags
        if (Files.exists(tagsPath)) {
            for (File file : tagsPath.toFile().listFiles()) {
                if (file.isDirectory()) {
                    try {
                        Tag tag = new ConfigurationTag(file);
                        tags.add(tag);
                    } catch (ParseException e) {
                        logger.error("Could not parse tag path: " + file.getPath());
                    }
                }
            }
        }

        return tags;
    }

    public void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }

    public void copy(Path source, Path destination) throws IOException {
        FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
}
