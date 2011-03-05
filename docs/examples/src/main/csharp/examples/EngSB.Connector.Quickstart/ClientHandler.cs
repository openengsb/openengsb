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

#region License

/*
 *  Copyright 2010 OpenEngSB Division, Vienna University of Technology
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
using System;
using EngSB.Connector.Common;
using System.Text;

namespace EngSB.Connector.Quickstart
{
	/// <summary>
	/// Description of ClientHandler.
	/// </summary>
	public class ClientHandler
	{
		private EngsbServiceRequestor service;
		
		public EngsbServiceRequestor Service {
			get { return service; }
			set { service = value; }
		}
		
		public ClientHandler()
		{
		}
		
		public void Send()
		{
			StringBuilder body = new StringBuilder();
       		body.Append("<query>").Append("*").Append("</query>");

        		EngsbMessageBuilder builder = EngsbMessageBuilder.CreateMessage("acmQueryRequestMessage").WithOwnFullNamespaces()
        			.SetMessageId(Guid.NewGuid()).SetTimestamp(DateTime.Now).SetBody(body.ToString())
                		.SetAlternativeRootNamespace("http://www.ifs.tuwien.ac.at/engsb/acm/query/request")
                		.SetAlternativeRootLocation("http://www.ifs.tuwien.ac.at/asb/acm-query-request.xsd");
        		
        		EngsbMessage msg = service.DoServiceCall(builder);
        		
        		Console.WriteLine(msg.Body.InnerText);
		}
	}
}
