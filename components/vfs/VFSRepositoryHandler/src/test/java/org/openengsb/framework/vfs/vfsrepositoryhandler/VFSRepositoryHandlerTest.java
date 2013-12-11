package org.openengsb.framework.vfs.vfsrepositoryhandler;

import org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations.FileOperator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.framework.vfs.api.repositoryhandler.RepositoryHandler;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openengsb.framework.vfs.api.common.Tag;
import org.openengsb.framework.vfs.vfsrepositoryhandler.tags.ConfigurationTag;
import org.osgi.framework.BundleContext;

public class VFSRepositoryHandlerTest {

    private ResourceBundle repositoryHandlerProperties = ResourceBundle.getBundle("repositoryhandler");
    private Path repositoryPath;
    private Path configurationPath;
    private Path tagsPath;
    private DateFormat tagsDateFormat = new SimpleDateFormat(repositoryHandlerProperties.getString("tags_date_format"));
    private BundleContext bundleContext;

    @Before
    public void setUp() {
        Path currentDirectory = (new File(".")).toPath();
        repositoryPath = currentDirectory.resolve(repositoryHandlerProperties.getString("repository_path"));
        configurationPath = repositoryPath.resolve(repositoryHandlerProperties.getString("configuration_path"));
        tagsPath = repositoryPath.resolve(repositoryHandlerProperties.getString("tags_path"));
        bundleContext = mock(BundleContext.class);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testRepositoryPaths() throws IOException {
        //TODO: Bundle Context = null, check if ok
        FileOperator fileOperator = mock(FileOperator.class);
        RepositoryHandler repositoryHandler = VFSRepositoryHandler.getInstance();
        ((VFSRepositoryHandler) repositoryHandler).setFileOperator(fileOperator);
        ((VFSRepositoryHandler) repositoryHandler).setBundleContext(bundleContext);
        ((VFSRepositoryHandler) repositoryHandler).start();

        verify(fileOperator).createDirectories(repositoryPath);
        verify(fileOperator).createDirectories(configurationPath);
        verify(fileOperator).createDirectories(tagsPath);

        Assert.assertEquals(repositoryPath, repositoryHandler.getRepositoryPath());
        Assert.assertEquals(configurationPath, repositoryHandler.getConfigurationPath());
    }

    @Test
    public void testTagDirectory() throws IOException {
        FileOperator fileOperator = mock(FileOperator.class);
        
        RepositoryHandler repositoryHandler = VFSRepositoryHandler.getInstance();
        ((VFSRepositoryHandler) repositoryHandler).setFileOperator(fileOperator);
        ((VFSRepositoryHandler) repositoryHandler).setBundleContext(bundleContext);
        ((VFSRepositoryHandler) repositoryHandler).start();

        String tagName = "testTag";
        Date tagDate = new Date();
        String expectedTagDirectoryName = tagsDateFormat.format(tagDate) + "_" + tagName;
        Path expectedTagPath = tagsPath.resolve(expectedTagDirectoryName);

        repositoryHandler.tagDirectory(configurationPath, tagName);

        verify(fileOperator).copy(configurationPath, expectedTagPath);
    }

    @Test
    public void testGetPreviousTagWithNoTagsPresent() throws ParseException {
        String tagName = "testTag";
        Date tagDate = new Date();
        String tagFilename = tagsDateFormat.format(tagDate) + "_" + tagName + ".zip";
        Path tagPath = tagsPath.resolve(tagFilename);
        Tag tag = new ConfigurationTag(tagPath);

        List<Tag> tags = new ArrayList<Tag>();

        FileOperator fileOperator = mock(FileOperator.class);
        when(fileOperator.getTagsFromDirectory(tagsPath)).thenReturn(tags);

        RepositoryHandler repositoryHandler = VFSRepositoryHandler.getInstance();
        ((VFSRepositoryHandler) repositoryHandler).setFileOperator(fileOperator);
        ((VFSRepositoryHandler) repositoryHandler).setBundleContext(bundleContext);
        ((VFSRepositoryHandler) repositoryHandler).start();

        Assert.assertNull(repositoryHandler.getPreviousTag(tag));
    }

    @Test
    public void testGetPreviousTagWithNoOlderTagsPresent() throws ParseException {
        String tagName = "testTag";
        Date tagDate = new Date();
        String tagFilename = tagsDateFormat.format(tagDate) + "_" + tagName + ".zip";
        Path tagPath = tagsPath.resolve(tagFilename);
        Tag tag = new ConfigurationTag(tagPath);

        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag);

        FileOperator fileOperator = mock(FileOperator.class);
        when(fileOperator.getTagsFromDirectory(tagsPath)).thenReturn(tags);

        RepositoryHandler repositoryHandler = VFSRepositoryHandler.getInstance();
        ((VFSRepositoryHandler) repositoryHandler).setFileOperator(fileOperator);
        ((VFSRepositoryHandler) repositoryHandler).setBundleContext(bundleContext);
        ((VFSRepositoryHandler) repositoryHandler).start();

        Assert.assertNull(repositoryHandler.getPreviousTag(tag));
    }

    @Test
    public void testGetPreviousTagWithTagsPresent() throws ParseException {
        String tag1Name = "testTag1";
        Date tag1Date = new Date(System.currentTimeMillis() - 2000);
        String tag1Filename = tagsDateFormat.format(tag1Date) + "_" + tag1Name + ".zip";
        Path tag1Path = tagsPath.resolve(tag1Filename);
        Tag tag1 = new ConfigurationTag(tag1Path);

        String tag2Name = "testTag2";
        Date tag2Date = new Date(System.currentTimeMillis() - 1000);
        String tag2Filename = tagsDateFormat.format(tag2Date) + "_" + tag2Name + ".zip";
        Path tag2Path = tagsPath.resolve(tag2Filename);
        Tag tag2 = new ConfigurationTag(tag2Path);

        String tag3Name = "testTag3";
        Date tag3Date = new Date();
        String tag3Filename = tagsDateFormat.format(tag3Date) + "_" + tag3Name + ".zip";
        Path tag3Path = tagsPath.resolve(tag3Filename);
        Tag tag3 = new ConfigurationTag(tag3Path);

        List<Tag> tags = new ArrayList<Tag>();
        tags.add(tag1);
        tags.add(tag2);
        tags.add(tag3);

        FileOperator fileOperator = mock(FileOperator.class);
        when(fileOperator.getTagsFromDirectory(tagsPath)).thenReturn(tags);

        RepositoryHandler repositoryHandler = VFSRepositoryHandler.getInstance();
        ((VFSRepositoryHandler) repositoryHandler).setFileOperator(fileOperator);
        ((VFSRepositoryHandler) repositoryHandler).setBundleContext(bundleContext);
        ((VFSRepositoryHandler) repositoryHandler).start();

        Tag returnedTag = repositoryHandler.getPreviousTag(tag3);
        Assert.assertEquals(tag2, returnedTag);
        returnedTag = repositoryHandler.getPreviousTag(returnedTag);
        Assert.assertEquals(tag1, returnedTag);
        Assert.assertNull(repositoryHandler.getPreviousTag(returnedTag));
    }
}
