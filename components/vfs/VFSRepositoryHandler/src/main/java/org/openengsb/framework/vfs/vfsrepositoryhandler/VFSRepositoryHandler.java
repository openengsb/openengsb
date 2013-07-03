package org.openengsb.framework.vfs.vfsrepositoryhandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;
import org.openengsb.framework.vfs.configurationserviceapi.configurationservice.ConfigurationService;
import org.openengsb.framework.vfs.configurationserviceapi.repositoryhandler.RepositoryHandler;
import org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations.FileOperator;
import org.openengsb.framework.vfs.vfsrepositoryhandler.fileoperations.VFSFileOperator;
import org.openengsb.framework.vfs.vfsrepositoryhandler.servicelistener.ConfigurationServiceListener;
import org.openengsb.framework.vfs.vfsrepositoryhandler.tags.ConfigurationTag;
import org.openengsb.framework.vfs.vfsrepositoryhandler.tags.ConfigurationTags;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VFSRepositoryHandler implements RepositoryHandler {

    private static VFSRepositoryHandler instance;
    private final Logger logger = LoggerFactory.getLogger(VFSRepositoryHandler.class);
    private ResourceBundle repositoryHandlerProperties = ResourceBundle.getBundle("repositoryhandler");
    private Path repositoryPath;
    private Path configurationPath;
    private Path tagsPath;
    private ConfigurationTags tags;
    private DateFormat tagsDateFormat;
    private ConfigurationService configurationService = null;
    private ConfigurationServiceListener configurationServiceListener = null;
    private BundleContext bundleContext;
    private FileOperator fileOperator;

    private VFSRepositoryHandler() {
        fileOperator = new VFSFileOperator();

        Path currentDirectory = (new File(".")).toPath();
        repositoryPath = currentDirectory.resolve(repositoryHandlerProperties.getString("repository_path"));
        configurationPath = repositoryPath.resolve(repositoryHandlerProperties.getString("configuration_path"));
        tagsPath = repositoryPath.resolve(repositoryHandlerProperties.getString("tags_path"));

        tagsDateFormat = new SimpleDateFormat(repositoryHandlerProperties.getString("tags_date_format"));

        tags = new ConfigurationTags();
    }

    public static VFSRepositoryHandler getInstance() {
        if (instance == null) {
            instance = new VFSRepositoryHandler();
        }

        return instance;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setFileOperator(FileOperator fileZipper) {
        this.fileOperator = fileZipper;
    }

    public void start() {
        configurationServiceListener = new ConfigurationServiceListener(bundleContext, this);
        configurationServiceListener.open();

        try {
            fileOperator.createDirectories(repositoryPath);
            fileOperator.createDirectories(configurationPath);
            fileOperator.createDirectories(tagsPath);
        } catch (IOException e) {
            logger.error("Could not create repository directories: " + e.getMessage());
        }

        readExistingTags();
    }

    public void tagDirectory(Path path, String tagName) {
        logger.debug("tagDirectory");

        Date tagDate = new Date();
        String tagDirectoryName = tagsDateFormat.format(tagDate) + "_" + tagName;
        Path tagPath = tagsPath.resolve(tagDirectoryName);
        
        try {
            fileOperator.copy(path, tagPath);
        } catch (IOException e) {
            logger.error("Could not copy tag to destination: " + e.getMessage());
        }

        Tag tag = new ConfigurationTag(tagName, tagPath, tagDate);
        tags.addTag(tag);

        if (configurationService != null) {
            logger.debug("executing newTag in ConfigurationService");
            configurationService.newTag(tag);
        }
    }

    public Tag getPreviousTag(Tag currentTag) {
        return tags.getPreviousTag(currentTag);
    }

    public Path getRepositoryPath() {
        return repositoryPath;
    }

    public Path getConfigurationPath() {
        return configurationPath;
    }

    public void registerConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ConfigurationService getConfigurationService() {
        return this.configurationService;
    }

    public void deregisterConfigurationService() {
        configurationService = null;
    }

    private void readExistingTags() {
        tags = new ConfigurationTags();
        for (Tag tag : fileOperator.getTagsFromDirectory(tagsPath)) {
            tags.addTag(tag);
        }
    }
}
