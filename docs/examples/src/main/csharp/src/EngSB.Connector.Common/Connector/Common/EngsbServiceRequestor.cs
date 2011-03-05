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
using Apache.NMS;
using System;
using System.Threading;
using Common.Logging;
using Spring.Messaging.Nms.Core;
#endregion

namespace EngSB.Connector.Common
{
	/// <summary>
	/// Description of EngsbServiceRequestor.
	/// </summary>
	public class EngsbServiceRequestor
	{
    		#region Logger
    		private ILog log = LogManager.GetLogger(typeof(EngsbServiceRequestor));
    		#endregion
    		
    		#region Properties
   		private EngsbMessageWorker worker;
   		private NmsTemplate nmsTemplate;
   		#endregion
    		
    		#region Accessors
    		public NmsTemplate NmsTemplate {
			get { return nmsTemplate; }
			set { nmsTemplate = value; }
		}
    		public EngsbMessageWorker Worker {
			get { return worker; }
			set { worker = value; }
		}
    		#endregion

    		public EngsbMessage DoServiceCall(EngsbMessageBuilder builder)
    		{
        		this.log.Trace("Message abstractor used to send the message...");
        		
        		this.worker.MessageToSend = builder.Finish();
        		return (EngsbMessage) this.nmsTemplate.Execute(this.worker, true);
    		}
	}
}
