package org.openengsb.framework.vfs.vfsconfigurationservice.fileoperations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileOperator {
    List<String> compareFolders(File originalPath, File newPath);

    void fileDelete(File srcFile);

    void copy(Path source, Path destination) throws IOException;

    void createDirectories(Path path) throws IOException;
    
    File[] listFiles(File file);
}
