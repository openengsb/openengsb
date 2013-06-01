package org.openengsb.framework.vfs.vfsrepositoryhandler.tags;

import java.io.File;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationTag implements Tag
{
	private final Logger logger = LoggerFactory.getLogger(ConfigurationTag.class);
	private ResourceBundle configurationServiceProperties = ResourceBundle.getBundle("repositoryhandler");
	DateFormat dateFormat = new SimpleDateFormat(configurationServiceProperties.getString("tags_date_format"));
	private String name;
	private Path path;
	private Date date;

	public ConfigurationTag(String name, Path path, Date date)
	{
		this.name = name;
		this.path = path;
		this.date = date;
	}

	public ConfigurationTag(Path path) throws ParseException
	{
		this.path = path;
		parseNameAndDate(path.toFile());
	}

	public ConfigurationTag(File file) throws ParseException
	{
		path = file.toPath();
		parseNameAndDate(file);
	}

	public String getName()
	{
		return name;
	}

	public Path getPath()
	{
		return path;
	}

	public Date getDate()
	{
		return date;
	}

	public int compareTo(Tag that)
	{
		return date.compareTo(that.getDate());
	}

	@Override
	public boolean equals(Object that)
	{
		if (that == null)
		{
			return false;
		}

		if (!(that instanceof ConfigurationTag))
		{
			return false;
		}

		ConfigurationTag tag = (ConfigurationTag) that;
		return (name.equals(tag.name) && date.equals(tag.date));
	}

	private void parseNameAndDate(File file) throws ParseException
	{
		String fileName = file.getName();
		int splitIndex = fileName.indexOf("_", fileName.indexOf("_") + 1);
		name = fileName.substring(splitIndex + 1, fileName.length());

		date = dateFormat.parse(fileName);
	}
}