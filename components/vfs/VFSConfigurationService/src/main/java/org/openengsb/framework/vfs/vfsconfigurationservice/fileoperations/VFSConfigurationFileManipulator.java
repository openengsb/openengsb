package org.openengsb.framework.vfs.vfsconfigurationservice.fileoperations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VFSConfigurationFileManipulator implements ConfigurationFileManipulator {
    private final Logger logger = LoggerFactory.getLogger(VFSConfigurationFileManipulator.class);

    public List<String> compareFolders(File originalPath, File newPath) {
        ArrayList<String> changedFiles = new ArrayList<String>();

        List<String> originalFolderContent = listPath(originalPath);
        List<String> newFolderContent = listPath(newPath);

        String originalFolderPath = originalPath.toString();
        String newFolderPath = newPath.toString();

        for (String newFilePath : newFolderContent) {
            String originalFilePath = newFilePath.replace(newFolderPath, originalFolderPath);

            if (originalFolderContent.contains(originalFilePath)) {
                File originalFile = new File(originalFilePath);
                if (Files.isDirectory(originalFile.toPath())) {
                    originalFolderContent.remove(originalFilePath);
                } else {
                    File newFile = new File(newFilePath);
                    if (!compareFiles(originalFile, newFile)) {
                        changedFiles.add(newFilePath);
                    }

                    originalFolderContent.remove(originalFilePath);
                }
            } else {
                changedFiles.add(newFilePath);
            }
        }

        for (String originalFilePath : originalFolderContent) {
            //ToDo is there a better way to resolve the relative path?
            String missingFilePath = originalFilePath.replace(originalFolderPath, newFolderPath);
            changedFiles.add(missingFilePath);
        }

        return changedFiles;
    }

    public void fileDelete(File srcFile) {
        // Checks if file is a directory
        if (srcFile.isDirectory()) {
            //Gathers files in directory
            for (File f : srcFile.listFiles()) {
                //Recursively deletes all files and sub-directories
                fileDelete(f);
            }
            // Deletes original sub-directory file
            srcFile.delete();
        } else {
            srcFile.delete();
        }
    }

    public void copyDirectory(File originalPath, File newPath) {
        try {
            FileUtils.copyDirectory(originalPath, newPath);
        } catch (IOException e) {
            logger.error("Could not copy directory: " + e.getMessage());
        }
    }

    private List<String> listPath(File path) {
        List<String> allFiles = new ArrayList<String>();

        File[] files = path.listFiles();

        if (files.length > 0) {
            Arrays.sort(files);
            for (int i = 0; i < files.length; i++) {
                allFiles.add(files[i].toString());

                if (files[i].isDirectory()) {
                    allFiles.addAll(listPath(files[i]));
                }
            }
        }

        return allFiles;
    }

    private boolean compareFiles(File originalFile, File newFile) {
        try {
            FileInputStream originalFileStream = new FileInputStream(originalFile);
            FileInputStream newFileStream = new FileInputStream(newFile);

            String originalFileHash = DigestUtils.md5Hex(originalFileStream);
            String newFileHash = DigestUtils.md5Hex(newFileStream);

            originalFileStream.close();
            newFileStream.close();

            return originalFileHash.equals(newFileHash);
        } catch (FileNotFoundException e) {
            //should not be reachable since just existing files are processed
            logger.error("Could not compare Files: " + e.getMessage());
        } catch (IOException e) {
            //should not be reachable since just existing files are processed
            logger.error("Could not compare Files: " + e.getMessage());
        }

        return false;
    }

    @Override
    public void copy(Path source, Path destination) throws IOException {
        FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }

    @Override
    public void createDirectories(Path path) throws IOException {
        Files.createDirectories(path);
    }
    
    @Override
    public File[] listFiles(File file) {
        return file.listFiles();
    }
}
