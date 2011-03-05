#region License

/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#endregion

#region Imports
using System;
using System.IO;
using System.Reflection;

using NUnit.Framework;

#endregion

namespace EngSB.Connector.Common
{
	/// <summary>
	/// Tests if messages are correctly extracted from strings.
	/// </summary>
	[TestFixture]
	public class EngsbMessageTest
	{
		/// <summary>
		/// Tests if the EngsbMessage extracts the right values out of a message.
		/// </summary>
		[Test]
		public void TestParsingMessage()
		{
			Assembly assembly = Assembly.GetExecutingAssembly();
 			TextReader textReader = new StreamReader(assembly.GetManifestResourceStream("TestMessage"));
 			string file = textReader.ReadToEnd();
 			textReader.Close();
			
			EngsbMessage msg = EngsbMessage.CreateFromXml(file);
			Assert.AreEqual(msg.MessageId, new Guid("5ff89772-0e20-44bd-9a97-d022ec2680db"));
			Assert.AreEqual(msg.Timestamp, new DateTime(2001,12,31,12,0,0,065,DateTimeKind.Local));
			Assert.AreEqual(msg.ParentMessageId, new Guid("2694c426-7908-48ab-98c3-2601f31de2a7"));
			Assert.AreEqual(msg.CorrelationId, new Guid("2694c426-7908-48ab-98c3-2601f31de2a7"));
		}
		
		/// <summary>
		/// In the case of an empty message an EngsbException should be thrown.
		/// </summary>
		[Test]
		[ExpectedException(typeof(EngsbException))]
		public void TestEmptyMessageFailure(){
			EngsbMessage.CreateFromXml("");
		}
		
		/// <summary>
		/// In the case of an empty message an EngsbException should be thrown.
		/// </summary>
		[Test]
		[ExpectedException(typeof(EngsbException))]
		public void TestNullMessageFailure(){
			EngsbMessage.CreateFromXml(null);
		}
	}
}
