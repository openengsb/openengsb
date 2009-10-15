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
		/** Default timeout used if no other is configured. */
    		private static readonly int TIMEOUT = 10000;

    		#region Logger
    		private ILog log = LogManager.GetLogger(typeof(EngsbServiceRequestor));
    		#endregion
    		
    		#region Properties
   		private NmsTemplate nmsTemplate;
   		private string destination;
    		private EngsbMessage response;
    		private Guid messageId;
    		private Object monitor = new Object();
		#endregion
    		
    		#region Accessors
    		
    		public NmsTemplate NmsTemplate
    		{
			get { return nmsTemplate; }
			set { nmsTemplate = value; }
		}
    		
    		public string Destination
    		{
			get { return destination; }
			set { destination = value; }
		}
    		
    		#endregion
    		
    		public EngsbServiceRequestor() {}

    		public EngsbMessage DoServiceCall(EngsbMessageBuilder builder)
    		{
        		this.log.Info("Doing servicecall with default timeout...");
        		return DoServiceCall(builder, TIMEOUT);
    		}

    		public EngsbMessage DoServiceCall(EngsbMessageBuilder builder, int timeout) 
    		{
        		this.log.Trace("Message abstractor used to send the message...");
        		this.log.Info("Doing service call with [" + timeout + "]ms timeout...");

        		builder.SetReplyQueue(Guid.NewGuid().ToString());
        		this.messageId = builder.GetInnerMessage().MessageId;

        		string toSend = builder.Finish();
        		this.log.Debug("Sending message to client");
        		this.nmsTemplate.SendWithDelegate( this.destination, delegate(ISession session) {return session.CreateTextMessage(toSend); });
                    		
        		lock(this)
        		{
        			Monitor.Wait(this);
        		}

        		if (this.response == null) throw new EngsbServiceException("Response message is null, because of a timeout...");

        		return this.response;
    		}

    
    		public void MessageReceived(IMessage message) 
    		{
       		 this.log.Trace("Message received on service requestor...");
        		if (message is ITextMessage) 
        		{
            		try 
            		{
                			this.log.Debug("Message is a textmessage with content [" + ((ITextMessage) message).Text + "]...");
                			EngsbMessage msg = EngsbMessage.CreateFromXml(((ITextMessage) message).Text);
                			if (!(this.messageId == msg.CorrelationId))
               			 {
                    			this.log.Debug("Received message with correlation id [" + msg.CorrelationId + "] which does not match messageId [" + this.messageId+ "]...");
               			 }
               			 this.log.Debug("Message received [" + msg.ToString() + "] and lock released...");
                   		 this.response = msg;
                   		 lock(this)
                   		 {
                   		 	Monitor.PulseAll(this);
                   		 }
               		}
           			catch (Exception ex) 
           			{
                			throw new EngsbServiceException("Cant retrieve message...",ex);
            		}
       		}
        		else 
        		{
            		throw new EngsbServiceException("Message must be of type TextMessage");
        		}
    		}
	}
}
