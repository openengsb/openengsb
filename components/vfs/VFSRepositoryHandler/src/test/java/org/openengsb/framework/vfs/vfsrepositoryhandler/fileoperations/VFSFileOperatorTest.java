package org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;
import org.junit.Assert;

public class VFSFileOperatorTest {

    private ResourceBundle repositoryHandlerProperties = ResourceBundle.getBundle("repositoryhandler");
    private DateFormat tagsDateFormat = new SimpleDateFormat(repositoryHandlerProperties.getString("tags_date_format"));
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
    public void testGetTagsFromDirectoryEmptyDirectory() throws IOException {
        String tagsDirectoryName = "TagsTestDirectory";
        Path tagsDirectory = (new File(".")).toPath().resolve(tagsDirectoryName);

        if (Files.exists(tagsDirectory)) {
            FileUtils.deleteDirectory(tagsDirectory.toFile());
        }

        Assert.assertFalse(Files.exists(tagsDirectory));
        Files.createDirectories(tagsDirectory);

        List<Tag> tags = fileOperator.getTagsFromDirectory(tagsDirectory);

        Assert.assertNotNull(tags);
        Assert.assertEquals(0, tags.size());

        FileUtils.deleteDirectory(tagsDirectory.toFile());
    }

    @Test
    public void testGetTagsFromDirectoryTagsInDirectory() throws IOException, ParseException {
        String tagsDirectoryName = "TagsTestDirectory";
        Path tagsDirectory = (new File(".")).toPath().resolve(tagsDirectoryName);

        if (Files.exists(tagsDirectory)) {
            FileUtils.deleteDirectory(tagsDirectory.toFile());
        }

        String tag1DateString = "20130102_121514";
        Date tag1Date = tagsDateFormat.parse(tag1DateString);
        String tag1Name = "tag1";
        String tag1DirectoryName = tag1DateString + "_" + tag1Name;
        Path tag1Directory = tagsDirectory.resolve(tag1DirectoryName);

        String tag2DateString = "20130103_121514";
        Date tag2Date = tagsDateFormat.parse(tag2DateString);
        String tag2Name = "tag2";
        String tag2DirectoryName = tag2DateString + "_" + tag2Name;
        Path tag2Directory = tagsDirectory.resolve(tag2DirectoryName);

        Assert.assertFalse(Files.exists(tagsDirectory));
        Files.createDirectories(tagsDirectory);
        Files.createDirectories(tag1Directory);
        Files.createDirectories(tag2Directory);

        List<Tag> tags = fileOperator.getTagsFromDirectory(tagsDirectory);

        Assert.assertNotNull(tags);
        Assert.assertEquals(2, tags.size());


        for (Tag t : tags) {
            if (t.getName().equals(tag1Name)) {
                Assert.assertEquals(tag1Date, t.getDate());
                Assert.assertEquals(tag1Directory, t.getPath());
            } else if (t.getName().equals(tag2Name)) {
                Assert.assertEquals(tag2Date, t.getDate());
                Assert.assertEquals(tag2Directory, t.getPath());
            } else {
                Assert.fail("Unknown tag name in tag list");
            }
        }

        FileUtils.deleteDirectory(tagsDirectory.toFile());
    }

    @Test
    public void testGetTagsFromDirectoryUnparseableTags() throws IOException {
        String tagsDirectoryName = "TagsTestDirectory";
        Path tagsDirectory = (new File(".")).toPath().resolve(tagsDirectoryName);

        String unparseableTagDirectoryName = "UnparseableTagString";
        Path unparseableTagDirectory = tagsDirectory.resolve(unparseableTagDirectoryName);

        if (Files.exists(tagsDirectory)) {
            FileUtils.deleteDirectory(tagsDirectory.toFile());
        }

        Assert.assertFalse(Files.exists(tagsDirectory));
        Files.createDirectories(tagsDirectory);
        Files.createDirectories(unparseableTagDirectory);

        List<Tag> tags = fileOperator.getTagsFromDirectory(tagsDirectory);

        Assert.assertNotNull(tags);
        Assert.assertEquals(0, tags.size());

        FileUtils.deleteDirectory(tagsDirectory.toFile());
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
