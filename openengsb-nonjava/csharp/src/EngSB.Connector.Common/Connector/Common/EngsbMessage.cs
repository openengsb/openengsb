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
using System.Collections;
using Common.Logging;
using System.Collections.Generic;
using System.Xml;

#endregion

namespace EngSB.Connector.Common
{
	/// <summary>
	/// Java Bean describing the base structure of an engsb message. Only the header
	/// and all relevant fields are shown here, but not the data itself. This is only
	/// transported and shown in an xml string created with the
	/// <see cref="EngSB.Connector.Common.EngsbMessageBuilder">EngsbMessageBuilder</see> 
	/// and the <see cref="EngSB.Connector.Common.EngsbMessage">EngsbMessage</see> as an helper.
	/// </summary>
	public class EngsbMessage
	{
		#region Logger
		private ILog log = LogManager.GetLogger(typeof(EngsbMessage));
		#endregion
		
		#region Properties
		
		private string messageType;
		private Guid messageId;
		private Guid parentMessageId;
		private DateTime timestamp;
		private Guid correlationId;
		private string replyQueue;
		private XmlElement body;
		
		#endregion
		
		#region Accessors
		
		public string MessageType 
		{
			get { return messageType; }
			set { messageType = value; }
		}
		
		public Guid MessageId 
		{
			get { return messageId; }
			set { messageId = value; }
		}
		
		public Guid ParentMessageId 
		{
			get { return parentMessageId; }
			set { parentMessageId = value; }
		}
		
		public DateTime Timestamp 
		{
			get { return timestamp; }
			set { timestamp = value; }
		}
		
		public Guid CorrelationId 
		{
			get { return correlationId; }
			set { correlationId = value; }
		}
		
		public string ReplyQueue {
			get { return replyQueue; }
			set { replyQueue = value; }
		}
		
		public XmlElement Body {
			get { return body; }
			set { body = value; }
		}
		
		#endregion
		
		public static EngsbMessage CreateFromXml(String xml)
		{
			if(String.IsNullOrEmpty(xml)) throw new EngsbException("Cant create message from null or empty string...");
			
			EngsbMessage msg = new EngsbMessage();
			
			msg.log.Debug("Creating message from\n"+xml);
			
        		try
        		{
            		XmlDocument doc = new XmlDocument();
            		doc.LoadXml(xml);
            		
            		XmlElement header = doc.DocumentElement["header"];
            		XmlElement body = doc.DocumentElement["body"];
            		       		
            		msg.MessageType = doc.DocumentElement.Name;
            		msg.log.Debug("MessageType: "+msg.messageType);
            		msg.MessageId = new Guid(header["base:messageId"].InnerText.Trim());
            		msg.log.Debug("MessageId: "+msg.messageId);
            		msg.Timestamp = DateTime.Parse(header["base:timestamp"].InnerText.Trim());
            		msg.log.Debug("Timestamp: "+msg.timestamp);
            		
            		XmlElement tmp = header["base:correlationId"];
            		if (tmp != null)
            		{
            			msg.CorrelationId = new Guid(tmp.InnerText.Trim());
            			msg.log.Debug("CorrelationId: "+msg.correlationId);
            		}
            		
            		tmp = header["base:parentMessageId"];
            		if (tmp != null) 
            		{
            			msg.ParentMessageId = new Guid((tmp.InnerText.Trim()));
            			msg.log.Debug("ParentMessageId: " + msg.parentMessageId);
            		}
            		
            		tmp = header["base:replyQueue"];
            		if (tmp != null) 
            		{
            			msg.ReplyQueue = tmp.InnerText.Trim();
            			msg.log.Debug("replyQueue: " + msg.replyQueue);
            		}
            
            		msg.body = body;
            		msg.log.Debug("Body: " + msg.body);
            
            		return msg;
        		} catch (Exception e) {
            		throw new EngsbException("parsing the xml failed", e);
        		}
    		}
	}
}
