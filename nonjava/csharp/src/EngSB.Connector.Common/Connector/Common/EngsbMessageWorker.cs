#region License

/*
 *  Copyright 2009 EngSB Team QSE/IFS, Vienna University of Technology
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
using Apache.NMS;
using Spring.Messaging.Nms.Core;
using log4net;

namespace EngSB.Connector.Common
{
	/// <summary>
	/// Description of MessageWorker.
	/// </summary>
	public class EngsbMessageWorker : ISessionCallback
	{
		#region Logger
    		private ILog log = LogManager.GetLogger(typeof(EngsbMessageWorker));
    		#endregion
		
		#region Properties
		private String edbDestination;
		private NmsTemplate nmsTemplate;
		private String reponseBase;
		private String messageToSend;
		#endregion
		
		#region Accessor
		public string EdbDestination {
			get { return edbDestination; }
			set { edbDestination = value; }
		}
		public string ReponseBase {
			get { return reponseBase; }
			set { reponseBase = value; }
		}
		public string MessageToSend {
			get { return messageToSend; }
			set { messageToSend = value; }
		}
		public NmsTemplate NmsTemplate {
			get { return nmsTemplate; }
			set { nmsTemplate = value; }
		}
		#endregion
		
		object ISessionCallback.DoInNms(ISession session)
		{
			this.log.Debug("Creating destinations...");
			IDestination edbDestination = CreateEdbDestinationQueueDestination(session);
			IDestination responseDestination = CreateEdbResponseQueueDestination(session);
				
			this.log.Debug("Creating Message to Send");
			ITextMessage msg = session.CreateTextMessage(messageToSend);
			msg.NMSReplyTo = responseDestination;
			
			this.log.Info("Sending message...");
			session.CreateProducer(edbDestination).Send(msg);
			this.log.Info("Receiving and transforming message...");
			return EngsbMessage.CreateFromXml(((ITextMessage)session.CreateConsumer(responseDestination).Receive()).Text);
		}
		
		private IDestination CreateEdbDestinationQueueDestination(ISession session)
		{
			return nmsTemplate.DestinationResolver.ResolveDestinationName(session,this.edbDestination,false);
		}
		
		private IDestination CreateEdbResponseQueueDestination(ISession session)
		{
			String createdDestinationName = "org.openengsb.edb"+ Guid.NewGuid();
			return nmsTemplate.DestinationResolver.ResolveDestinationName(session,createdDestinationName, false);
		}
	}
}
