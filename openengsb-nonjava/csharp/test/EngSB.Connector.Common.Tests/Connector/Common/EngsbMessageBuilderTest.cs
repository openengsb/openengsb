#region License

/*
 *  Copyright 2009 OpenEngSB Division, Vienna University of Technology
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

#endregion

#region Imports
using System;
using System.IO;
using System.Reflection;

using NUnit.Framework;
using System.Text;

#endregion

namespace EngSB.Connector.Common
{
	/// <summary>
	/// Description of EngsbMessageBuilderTest.
	/// </summary>
	[TestFixture]
	public class EngsbMessageBuilderTest
	{
		[Test]
		public void testCreateMessage() 
		{
			StringBuilder body = new StringBuilder();
				body.Append("<type>").Append("task").Append("</type>\n");
  				body.Append("<ownerId>").Append("asb").Append("</ownerId>\n");
  				body.Append("<description>").Append("the description").Append("</description>\n");
  				body.Append("<summary><![CDATA[").Append("one-liner summary").Append("]]></summary>\n");
  				body.Append("<componentId>").Append("logi.doc").Append("</componentId>\n");
			
			string xml = EngsbMessageBuilder.CreateMessage("IssueCreate").WithSchemasAndNamespace().SetMessageId(
  				new Guid("5ff89772-0e20-44bd-9a97-d022ec2680db")).SetTimestamp(
				new DateTime(2001,12,31)).SetParentId(new Guid("2694c426-7908-48ab-98c3-2601f31de2a7")).SetCorrelationId(
  				new Guid("2694c426-7908-48ab-98c3-2601f31de2a7")).SetBody(body.ToString()).Finish();
  				
  				Console.WriteLine(xml);
		}
	}
}
