package org.openengsb.framework.vfs.vfsconfigurationservice.fileoperations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.Assert;

public class VFSFileOperatorTest {
    private String testDirectoryName = "TestDirectory";
    private String testSubDirectoryName = "TestSubDirectory";
    private String testDirectoryFileName = "testFile.txt";
    private String testSubDirectoryFile1Name = "testSubFile1.txt";
    private String testSubDirectoryFile2Name = "testSubFile2.txt";
    private Path testDirectory = (new File(".")).toPath().resolve(testDirectoryName);
    private Path testSubDirectory = testDirectory.resolve(testSubDirectoryName);
    private Path testDirectoryFile = testDirectory.resolve(testDirectoryFileName);
    private Path testSubDirectoryFile1 = testSubDirectory.resolve(testSubDirectoryFile1Name);
    private Path testSubDirectoryFile2 = testSubDirectory.resolve(testSubDirectoryFile2Name);
    private FileOperator fileOperator = new VFSFileOperator();

    @Test
    public void testCompareFoldersEmptyFolders() throws IOException {
        Path folder1 = (new File(".")).toPath().resolve("TestFolder1");
        Path folder2 = (new File(".")).toPath().resolve("TestFolder2");

        Files.createDirectories(folder1);
        Files.createDirectories(folder2);

        List<String> list = fileOperator.compareFolders(folder1.toFile(), folder2.toFile());
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());

        FileUtils.deleteDirectory(folder1.toFile());
        FileUtils.deleteDirectory(folder2.toFile());
    }

    @Test
    public void testCompareFoldersEverythingMatches() throws IOException {
        createTestDirectory();

        Path destinationDirectory = (new File(".")).toPath().resolve("TestDestination");
        FileUtils.copyDirectory(testDirectory.toFile(), destinationDirectory.toFile());

        List<String> list = fileOperator.compareFolders(testDirectory.toFile(), destinationDirectory.toFile());
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());

        FileUtils.deleteDirectory(destinationDirectory.toFile());
        deleteTestDirectory();
    }

    @Test
    public void testCompareFoldersStuffAdded() throws IOException {
        createTestDirectory();

        String destinationDirectoryName = "TestDestination";
        Path destinationDirectory = (new File(".")).toPath().resolve(destinationDirectoryName);

        String testDirectoryFile2Name = "testFile2.txt";
        Path testDirectoryFile2 = destinationDirectory.resolve(testDirectoryFile2Name);

        String testSubDirectory2Name = "SubDirectory2";
        Path testSubDirectory2 = destinationDirectory.resolve(testSubDirectory2Name);

        String testSubDirectory2FileName = "sub2TestFile.txt";
        Path testSubDirectory2File = testSubDirectory2.resolve(testSubDirectory2FileName);

        FileUtils.copyDirectory(testDirectory.toFile(), destinationDirectory.toFile());

        Files.createDirectories(testSubDirectory2);
        createTestFile(testDirectoryFile2, testDirectoryFile2.toString());
        createTestFile(testSubDirectory2File, testSubDirectory2File.toString());

        Assert.assertTrue(Files.exists(testSubDirectory2));
        Assert.assertTrue(Files.exists(testDirectoryFile2));
        Assert.assertTrue(Files.exists(testDirectoryFile2));

        List<String> changedFiles = fileOperator.compareFolders(testDirectory.toFile(), destinationDirectory.toFile());
        Assert.assertNotNull(changedFiles);
        Assert.assertEquals(3, changedFiles.size());
        Assert.assertEquals(testSubDirectory2.toString(), changedFiles.get(0));
        Assert.assertEquals(testSubDirectory2File.toString(), changedFiles.get(1));
        Assert.assertEquals(testDirectoryFile2.toString(), changedFiles.get(2));

        FileUtils.deleteDirectory(destinationDirectory.toFile());
        deleteTestDirectory();
    }

    @Test
    public void testCompareFoldersFilesRemoved() throws IOException {
        createTestDirectory();

        Path destinationDirectory = (new File(".")).toPath().resolve("TestDestination");
        FileUtils.copyDirectory(testDirectory.toFile(), destinationDirectory.toFile());

        Path removedFile1 = destinationDirectory.resolve(testDirectoryFileName);
        Assert.assertTrue(Files.exists(removedFile1));
        Files.delete(removedFile1);
        Assert.assertFalse(Files.exists(removedFile1));

        Path removedFile2 = destinationDirectory.resolve(testSubDirectoryName).resolve(testSubDirectoryFile1Name);
        Assert.assertTrue(Files.exists(removedFile2));
        Files.delete(removedFile2);
        Assert.assertFalse(Files.exists(removedFile2));

        List<String> changedFiles = fileOperator.compareFolders(testDirectory.toFile(), destinationDirectory.toFile());
        Assert.assertNotNull(changedFiles);
        Assert.assertEquals(2, changedFiles.size());
        Assert.assertEquals(removedFile1.toString(), changedFiles.get(0));
        Assert.assertEquals(removedFile2.toString(), changedFiles.get(1));

        FileUtils.deleteDirectory(destinationDirectory.toFile());
        deleteTestDirectory();
    }

    @Test
    public void testCompareFoldersFoldersRemoved() throws IOException {
        createTestDirectory();

        Path destinationDirectory = (new File(".")).toPath().resolve("TestDestination");
        FileUtils.copyDirectory(testDirectory.toFile(), destinationDirectory.toFile());

        Path removedDirectory = destinationDirectory.resolve(testSubDirectoryName);
        Path removedFile1 = removedDirectory.resolve(testSubDirectoryFile1Name);
        Path removedFile2 = removedDirectory.resolve(testSubDirectoryFile2Name);

        Assert.assertTrue(Files.exists(removedDirectory));
        Assert.assertTrue(Files.exists(removedFile1));
        Assert.assertTrue(Files.exists(removedFile2));

        FileUtils.deleteDirectory(removedDirectory.toFile());

        Assert.assertFalse(Files.exists(removedDirectory));
        Assert.assertFalse(Files.exists(removedFile1));
        Assert.assertFalse(Files.exists(removedFile2));

        List<String> changedFiles = fileOperator.compareFolders(testDirectory.toFile(), destinationDirectory.toFile());
        Assert.assertNotNull(changedFiles);
        Assert.assertEquals(3, changedFiles.size());
        Assert.assertEquals(removedDirectory.toString(), changedFiles.get(0));
        Assert.assertEquals(removedFile1.toString(), changedFiles.get(1));
        Assert.assertEquals(removedFile2.toString(), changedFiles.get(2));

        FileUtils.deleteDirectory(destinationDirectory.toFile());
        deleteTestDirectory();
    }

    @Test
    public void testCompareFoldersFilesChanged() throws IOException {
        createTestDirectory();

        Path destinationDirectory = (new File(".")).toPath().resolve("TestDestination");
        FileUtils.copyDirectory(testDirectory.toFile(), destinationDirectory.toFile());

        Path changedFile1 = destinationDirectory.resolve(testDirectoryFileName);
        Assert.assertTrue(Files.exists(changedFile1));
        FileUtils.writeStringToFile(changedFile1.toFile(), "changed content");

        Path changedFile2 = destinationDirectory.resolve(testSubDirectoryName).resolve(testSubDirectoryFile1Name);
        Assert.assertTrue(Files.exists(changedFile2));
        FileUtils.writeStringToFile(changedFile2.toFile(), "changed content");

        List<String> changedFiles = fileOperator.compareFolders(testDirectory.toFile(), destinationDirectory.toFile());
        Assert.assertNotNull(changedFiles);
        Assert.assertEquals(2, changedFiles.size());
        Assert.assertEquals(changedFile1.toString(), changedFiles.get(0));
        Assert.assertEquals(changedFile2.toString(), changedFiles.get(1));

        FileUtils.deleteDirectory(destinationDirectory.toFile());
        deleteTestDirectory();
    }

    @Test
    public void testFileDeleteValidFolder() throws IOException {
        createTestDirectory();

        Assert.assertTrue(Files.exists(testSubDirectory));
        fileOperator.fileDelete(testSubDirectory.toFile());
        Assert.assertFalse(Files.exists(testSubDirectory));

        deleteTestDirectory();
    }

    @Test
    public void testFileDeleteValidFile() throws IOException {
        createTestDirectory();

        Assert.assertTrue(Files.exists(testDirectoryFile));
        fileOperator.fileDelete(testDirectoryFile.toFile());
        Assert.assertFalse(Files.exists(testDirectoryFile));

        deleteTestDirectory();
    }

    @Test
    public void testFileDeleteInvalidSource() throws IOException {
        Path invalidDirectory = (new File(".")).toPath().resolve("InvalidDirectory");
        Path invalidFile = (new File(".")).toPath().resolve("InvalidFile.txt");

        fileOperator.fileDelete(invalidDirectory.toFile());
        fileOperator.fileDelete(invalidFile.toFile());
    }

    @Test
    public void testCopyDirectorySourceValid() throws IOException {
        createTestDirectory();

        String targetTestDirectoryName = "CopiedTestDirectory";
        Path targetTestDirectory = (new File(".")).toPath().resolve(targetTestDirectoryName);
        Path targetTestSubDirectory = targetTestDirectory.resolve(testSubDirectoryName);
        Path targetTestestDirectoryFile = targetTestDirectory.resolve(testDirectoryFileName);
        Path targetTestSubDirectoryFile1 = targetTestSubDirectory.resolve(testSubDirectoryFile1Name);
        Path targetTestSubDirectoryFile2 = targetTestSubDirectory.resolve(testSubDirectoryFile2Name);

        fileOperator.copy(testDirectory, targetTestDirectory);

        Assert.assertTrue(Files.exists(targetTestDirectory));
        Assert.assertTrue(Files.exists(targetTestSubDirectory));
        Assert.assertTrue(Files.exists(targetTestestDirectoryFile));
        Assert.assertTrue(Files.exists(targetTestSubDirectoryFile1));
        Assert.assertTrue(Files.exists(targetTestSubDirectoryFile2));

        FileUtils.deleteDirectory(targetTestDirectory.toFile());

        deleteTestDirectory();
    }

    @Test
    public void testCopyDirectorySourceInvalid() {
        String targetTestDirectoryName = "CopiedTestDirectory";
        Path targetTestDirectory = (new File(".")).toPath().resolve(targetTestDirectoryName);
        Path targetTestSubDirectory = targetTestDirectory.resolve(testSubDirectoryName);
        Path targetTestestDirectoryFile = targetTestDirectory.resolve(testDirectoryFileName);
        Path targetTestSubDirectoryFile1 = targetTestSubDirectory.resolve(testSubDirectoryFile1Name);
        Path targetTestSubDirectoryFile2 = targetTestSubDirectory.resolve(testSubDirectoryFile2Name);

        Assert.assertFalse(Files.exists(testDirectory));

        try {
            fileOperator.copy(testDirectory, targetTestDirectory);
            Assert.fail("IOException should have been thrown!");
        } catch (IOException e) {
            // do nothing
        }

        Assert.assertFalse(Files.exists(targetTestDirectory));
        Assert.assertFalse(Files.exists(targetTestSubDirectory));
        Assert.assertFalse(Files.exists(targetTestestDirectoryFile));
        Assert.assertFalse(Files.exists(targetTestSubDirectoryFile1));
        Assert.assertFalse(Files.exists(targetTestSubDirectoryFile2));
    }

    private void createTestDirectory() throws IOException {
        if (Files.exists(testDirectory)) {
            deleteTestDirectory();
        }

        Files.createDirectories(testDirectory);
        Files.createDirectories(testSubDirectory);
        createTestFile(testDirectoryFile, testDirectoryFile.toString());
        createTestFile(testSubDirectoryFile1, testSubDirectoryFile1.toString());
        createTestFile(testSubDirectoryFile2, testSubDirectoryFile2.toString());

        Assert.assertTrue(Files.exists(testDirectory));
        Assert.assertTrue(Files.exists(testSubDirectory));
        Assert.assertTrue(Files.exists(testDirectoryFile));
        Assert.assertTrue(Files.exists(testSubDirectoryFile1));
        Assert.assertTrue(Files.exists(testSubDirectoryFile2));
    }

    private void deleteTestDirectory() throws IOException {
        if (Files.exists(testDirectory)) {
            FileUtils.deleteDirectory(testDirectory.toFile());
        }

        Assert.assertFalse(Files.exists(testDirectory));
        Assert.assertFalse(Files.exists(testSubDirectory));
        Assert.assertFalse(Files.exists(testDirectoryFile));
        Assert.assertFalse(Files.exists(testSubDirectoryFile1));
        Assert.assertFalse(Files.exists(testSubDirectoryFile2));
    }

    private void createTestFile(Path filePath, String content) throws IOException {
        FileUtils.writeStringToFile(filePath.toFile(), content);
    }
}
