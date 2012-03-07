/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ui.common;

import java.util.Locale;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.CompressedPackageResource;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;

public class FavIconPackageResource extends CompressedPackageResource{

	private static final long serialVersionUID = -8603235652736579379L;

	protected FavIconPackageResource(Class<?> scope, String path, Locale locale, String style)
	{
		super(scope, path, locale, style);
	}

	public static final HeaderContributor getHeaderContribution(final ResourceReference reference)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 7888632129467142236L;

			public void renderHead(IHeaderResponse response)
			{
				StringBuilder htmlTag=new StringBuilder();
			    htmlTag.append("<link rel=\"icon\" href=\"");
			    htmlTag.append("resources/"+reference.getSharedResourceKey());
			    htmlTag.append("\" type=\"image/x-icon\">\n");
			    response.renderString(htmlTag);
			}
		});
	}
	
	public static final HeaderContributor getHeaderContribution(final String location)
	{
		return new HeaderContributor(new IHeaderContributor()
		{
			private static final long serialVersionUID = 1L;

			public void renderHead(IHeaderResponse response)
			{
			    StringBuilder htmlTag=new StringBuilder();
			    htmlTag.append("<link rel=\"shortcut icon\" href=\"");
			    htmlTag.append(returnRelativePath(location));
			    htmlTag.append("\" type=\"image/x-icon\">\n");
			    response.renderString(htmlTag);
			}
		});
	}

	private static final String returnRelativePath(String location)
	{
		// WICKET-59 allow external URLs, WICKET-612 allow absolute URLs.
		if (location.startsWith("http://") || location.startsWith("https://") ||
			location.startsWith("/"))
		{
			return location;
		}
		else
		{
			return RequestCycle.get()
				.getProcessor()
				.getRequestCodingStrategy()
				.rewriteStaticRelativeUrl(location);
		}
	}

}
