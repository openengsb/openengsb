package org.openengsb.framework.vfs.vfsrepositoryhandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.karafDistributionConfiguration;

import javax.inject.Inject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.framework.vfs.configurationserviceapi.configurationservice.ConfigurationService;
import org.openengsb.framework.vfs.configurationserviceapi.repositoryhandler.RepositoryHandler;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

//@RunWith(JUnit4TestRunner.class)
//@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class VFSRepositoryHandlerIntegrationTest
{
	private ResourceBundle repositoryHandlerProperties = ResourceBundle.getBundle("repositoryhandler");
	
	private Path workingDirectory;
	private Path repositoryPath;
	private Path repositoryConfigurationPath;
	private Path repositoryTagsPath;
	
	private DateFormat tagsDateFormat;

	@Inject
	private BundleContext bundleContext;

	@Configuration
	public Option[] config()
	{
		return new Option[]
		{
			karafDistributionConfiguration().frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("zip").version("3.0.0.RC1")).karafVersion("3.0.0.RC1").name("Apache Karaf"),
			mavenBundle(maven().groupId("org.openengsb.framework.vfs.configurationserviceapi").artifactId("ConfigurationServiceAPI").version("1.0")),
			mavenBundle(maven().groupId("org.openengsb.framework.vfs.vfsconfigurationservice").artifactId("VFSConfigurationService").version("1.0")),
			mavenBundle(maven().groupId("org.openengsb.framework.vfs.vfsrepositoryhandler").artifactId("VFSRepositoryHandler").version("1.0"))
		};
	}
	
	@Before
	public void setUp()
	{
		workingDirectory = (new File(".")).toPath();
		repositoryPath = workingDirectory.resolve(repositoryHandlerProperties.getString("repository_path"));
		repositoryConfigurationPath = repositoryPath.resolve(repositoryHandlerProperties.getString("configuration_path"));
		repositoryTagsPath = repositoryPath.resolve(repositoryHandlerProperties.getString("tags_path"));
		
		tagsDateFormat = new SimpleDateFormat(repositoryHandlerProperties.getString("tags_date_format"));
	}
	
	@After
	public void tearDown()
	{
		//TODO cleanup
	}

	//@Test
	public void configurationServicePresent()
	{
		assertNotNull(retrieveConfigurationService());
	}

	//@Test
	public void repositoryHandlerPresent()
	{
		assertNotNull(retrieveRepositoryHandler());
	}

	//@Test
	public void tagDirectoryWithoutConfigurationServicePresent()
	{
		fail("not implemented yet");
		
		//TODO stop VFSConfigurationService bundle
		Bundle configurationServiceBundle = bundleContext.getBundle("VFSConfigurationService");
		assertNotNull(configurationServiceBundle);
		
		//TODO tag a directory
	}

	//@Test
	public void tagDirectoryWithConfigurationServicePresent()
	{
		assertNotNull(retrieveConfigurationService());
		
		RepositoryHandler repositoryHandler = retrieveRepositoryHandler();
		Path configurationPath = repositoryHandler.getConfigurationPath();
		
		String tagName = "testTag";
		Date now = new Date();
		repositoryHandler.tagDirectory(configurationPath, tagName);
		
		String expectedTagDirectoryName = tagsDateFormat.format(now) + "_" + tagName;
		Path expectedTagPath = repositoryTagsPath.resolve(expectedTagDirectoryName);
		assertTrue(Files.exists(expectedTagPath));
		
		//TODO test if ConfigurationService unpacked the tag successfully
	}

	private ConfigurationService retrieveConfigurationService()
	{
		ServiceReference reference = bundleContext.getServiceReference(ConfigurationService.class.getName());
		assertNotNull(reference);

		ConfigurationService service = (ConfigurationService) bundleContext.getService(reference);
		assertNotNull(service);

		return service;
	}

	private RepositoryHandler retrieveRepositoryHandler()
	{
		ServiceReference reference = bundleContext.getServiceReference(RepositoryHandler.class.getName());
		assertNotNull(reference);

		RepositoryHandler service = (RepositoryHandler) bundleContext.getService(reference);
		assertNotNull(service);

		return service;
	}
}
