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

using System;
using System.Collections.Generic;

namespace Connector
{
	/// <summary>
	/// Defines the response object which have to be sent to the OpenEngSB
	/// </summary>
	public class Response
	{
		public enum ReturnType {
			Void, Object, Exception,
		}

		public ReturnType type { get; set; }
		public object arg { get; set; }
		public IDictionary<string, string> metaData { get; set; }
		public string callId { get; set; }
		public string className { get; set; }
		
		public Response()
		{
			
		}
	}
}
