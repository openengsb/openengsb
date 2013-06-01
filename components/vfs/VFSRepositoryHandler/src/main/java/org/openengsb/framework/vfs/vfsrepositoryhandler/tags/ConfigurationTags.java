package org.openengsb.framework.vfs.vfsrepositoryhandler.tags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openengsb.framework.vfs.configurationserviceapi.common.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationTags
{

	private final Logger logger = LoggerFactory.getLogger(ConfigurationTags.class);
	private List<Tag> tags;

	public ConfigurationTags()
	{
		tags = new ArrayList<Tag>();
	}

	public List<Tag> getTags()
	{
		Collections.sort(tags);
		return tags;
	}

	public void addTag(Tag tag)
	{
		if (!tags.contains(tag))
		{
			tags.add(tag);
			Collections.sort(tags);
		}
	}

	@Override
	public boolean equals(Object that)
	{
		if (that == null)
		{
			return false;
		}

		if (!(that instanceof ConfigurationTags))
		{
			return false;
		}

		ConfigurationTags thatTags = (ConfigurationTags) that;

		List<Tag> thisTagList = getTags();
		List<Tag> thatTagList = thatTags.getTags();

		if (thisTagList.size() != thatTagList.size())
		{
			return false;
		}

		for (int i = 0; i < thisTagList.size(); i++)
		{
			if (!thisTagList.get(i).equals(thatTagList.get(i)))
			{
				return false;
			}
		}

		return true;
	}

	public Tag getPreviousTag(Tag tag)
	{
		Collections.sort(tags);

		int index = tags.indexOf(tag);

		if (index > 0)
		{
			return tags.get(index - 1);
		}

		return null;
	}
}