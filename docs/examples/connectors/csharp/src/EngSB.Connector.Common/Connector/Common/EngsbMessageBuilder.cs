/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

#region Imports
using System;
using System.Collections;
using Common.Logging;
using System.Text;

#endregion

namespace EngSB.Connector.Common
{
	/// <summary>
	/// Description of EngsbMessageBuilder.
	/// </summary>
	public class EngsbMessageBuilder
	{
		#region Logger
		private ILog log = LogManager.GetLogger(typeof(EngsbMessageBuilder));
		#endregion
					
		#region Constants
		private static readonly string BASE = "base";
   		private static readonly string SERVICE = "service";
   		private static readonly string ASB_BASE_NAMESPACE = "http://www.ifs.tuwien.ac.at/asb/";
    		private static readonly string BASE_NAMESPACE = "http://www.ifs.tuwien.ac.at/asb/AbstractMessage";
  		private static readonly string SERVICE_NAMESPACE = "http://www.ifs.tuwien.ac.at/engsb/AbstractServiceMessage";
   		private static readonly string BASE_NAMESPACE_LOCATION = "http://www.ifs.tuwien.ac.at/asb/AbstractMessage/abstract-message.xsd";
   		private static readonly string BASE_SERVICE_LOCATION = "http://www.ifs.tuwien.ac.at/asb/abstrac-service-message.xsd";    		
		#endregion
   		
     		//Object used to temporarly store the data required for building the
     		//message.
    		private EngsbMessage message = new EngsbMessage();
   		// /** Storing the namespaces used to build the message. */
   		private Hashtable namespaces = new Hashtable();
    		// /** Storing the locations mapped to the namespaces. */
    		private Hashtable namespaceLocations = new Hashtable();
    		// /** Body of the message containing the data to be sent. */
    		private string body;
    		/** An alternative root namespace. */
    		private string rootNamespace;
    		/** An alternative root location. */
    		private string rootLocation;
     		/**
    		 * Defines if a message have to be build with schemas and namespaces or
    		 * without.
    		 */
   		 private bool withSchemasAndNamespace;
    		/** Uses full own namespaces and does not set base ASB namespace before. */
    		private bool withOwnFullNamespaces;

   		private EngsbMessageBuilder() { }

    		public static EngsbMessageBuilder CreateMessage(string messageType)
    		{
        		EngsbMessageBuilder builder = new EngsbMessageBuilder();
        		builder.message.MessageType = messageType;
        		return builder;
    		}

    		public EngsbMessageBuilder WithSchemasAndNamespace() 
    		{
        		this.withSchemasAndNamespace = true;
        		return this;
    		}

    		/**
     		* This method is quite similar to the {@link #withSchemasAndNamespace()}
     		* method with the different that all namespaces set via the
     		* {@link #addNamespace(String, String)} method are seen as REAL namespaces
     		* and not only as parts added to the base ASB base.
     		*/
    		public EngsbMessageBuilder WithOwnFullNamespaces()
    		{
        		this.withOwnFullNamespaces = true;
        		this.withSchemasAndNamespace = true;
        		return this;
    		}

    		/**
     		* This method appends an own namespace to the message. Regularly namespaces
     		* are written in the form xmlns:namespaceIdentifier=namespace. This method
     		* allows two kinds of namespaces. If the {@link #withOwnFullNamespaces()}
     		* flag is set a full namespace is required. Otherwise its ok only putting
     		* the messagetype in which is appended to the asb base path.
     		*/
    		public EngsbMessageBuilder AddNamespace(string namespaceIdentifier, string namespc) 
    		{
    			this.namespaces.Add(namespaceIdentifier, namespc);
        		return this;
    		}

    		/**
     		* Appends an own namespace location. If a namespace is constructued added
     		* by the {@link #addNamespace(String, String)} method also the locations
     		* added by {@link #addNamespaceLocation(String, String)} is called and if
     		* one with the same namespaceIdentfier is found this location is used.
     		* Otherwise the pattern ASB_BASE_NAMESPACE plus messageType + .xsd is used.
     		*/
    		public EngsbMessageBuilder AddNamespaceLocation(string namespaceIdentifier, string namespaceLocation) 
    		{
        		this.namespaceLocations.Add(namespaceIdentifier, namespaceLocation);
        		return this;
    		}

    		public EngsbMessageBuilder SetMessageId(Guid id) {
        		this.message.MessageId = id;
        		return this;
    		}

    		public EngsbMessageBuilder SetParentId(Guid id)
    		{
        		this.message.ParentMessageId  = id;
       		return this;
   		 }

    		public EngsbMessageBuilder SetCorrelationId(Guid id)
    		{
        		this.message.CorrelationId = id;
        		return this;
   		 }

    		public EngsbMessageBuilder SetTimestamp(DateTime ts) 
    		{
        		this.message.Timestamp = ts;
        		return this;
  		}

		 public EngsbMessageBuilder SetBody(string body) 
		 {
		 	this.body = body;
		       return this;
		}

   		public EngsbMessageBuilder SetReplyQueue(string replyQueue)
   		{
       		 this.message.ReplyQueue = replyQueue;
        		return this;
    		}

	    	/**
	    	 * Actually the root namespace is build out of
	    	 * xmlns=\"http://www.ifs.tuwien.ac.at/asb/ plus the name of the message.
	    	 * Since this may not be enough or is defined in some user way this method
	    	 * allows to set an alternative namesapce completely different from the one
	    	 * presented.
	    	 */
	    	public EngsbMessageBuilder SetAlternativeRootNamespace(string nmspc)
	    	{
	       	this.rootNamespace = nmspc;
	        	return this;
	   	 }

		  /**
		   * Actually the root location (of the xsd file) is build out of
		   * xmlns=\"http://www.ifs.tuwien.ac.at/asb/ plus the name of the message
		   * plus .xsd. Nevertheless since its possible that the file is named in
		   * another way it have to be possible to add an own location at all.
		  */
		  public EngsbMessageBuilder SetAlternativeRootLocation(string location) 
		  {
		      this.rootLocation = location;
		      return this;
		  }
		
		  /**
		   * Access to the internal data without to analyse the created xml.
		   */
		  public EngsbMessage GetInnerMessage() 
		  {
		      return this.message;
		  }
		
		  /**
		   * This method is quite similar to the {@link #finish()} method with the
		   * difference that it used the "new" way to build messages. The difference
		   * is that also service messages are allowed which is completely ignored by
		   * the {@link #finish()} method and further more the explicit definition of
		   * namespaces which is also not allowed by the {@link #finish()} method.
		   */
		  public string Finish() 
		  {
		  	StringBuilder builder = new StringBuilder();
		  	char first = char.ToLower(this.message.MessageType[0]);
		       string rootName = first + this.message.MessageType.Substring(1);
		       builder.Append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
		       builder.Append("<");
		       builder.Append(rootName);
		       if (this.withSchemasAndNamespace) AppendSchemasAndNamespaceNew(builder);
		       builder.Append(">\n");
		       builder.Append("\t<header>\n");
		       AppendElement(builder, "messageId", this.message.MessageId.ToString());
		       AppendElement(builder, "timestamp", formatTimestamp(this.message.Timestamp));
		       if (this.message.ParentMessageId != null) AppendElement(builder, "parentMessageId", this.message.ParentMessageId.ToString());
		       if (this.message.CorrelationId != null)  AppendElement(builder, "correlationId", this.message.CorrelationId.ToString());
		       if (!String.IsNullOrEmpty(this.message.ReplyQueue))  AppendServiceMessageHeader(builder, "replyQueue", this.message.ReplyQueue);
		       builder.Append("  </header>\n");
		       builder.Append("  <body>\n");
		       builder.Append(this.body);
		       builder.Append("  </body>\n");
		       builder.Append("</");
		       builder.Append(rootName);
		       builder.Append(">");
		
		       String finalXml = builder.ToString();
		       this.log.Debug(finalXml);
		       return finalXml;
		    }
		
		    private string formatTimestamp(DateTime timestamp) {
		  	return timestamp.ToString("yyyy-MM-ddTHH:mm:ss.fff+01:00");
		    }
		
		    private void AppendElement(StringBuilder builder, String name, String value) {
		        GeneralAppend(builder, name, value, BASE);
		    }
		
		    private void AppendServiceMessageHeader(StringBuilder builder, String name, String value) {
		        GeneralAppend(builder, name, value, SERVICE);
		    }
		
		    private void GeneralAppend(StringBuilder builder, String name, String value, String type) {
		        builder.Append("\t\t<").Append(type).Append(":");
		        builder.Append(name);
		        builder.Append(">");
		        builder.Append(value);
		        builder.Append("</").Append(type).Append(":");
		        builder.Append(name);
		        builder.Append(">\n");
		    }
		
		    private void AppendSchemasAndNamespaceNew(StringBuilder builder) {
		        builder.Append(" xmlns=").Append("\"");
		        if (String.IsNullOrEmpty(this.rootNamespace))
		        {
		            builder.Append(ASB_BASE_NAMESPACE).Append(this.message.MessageType);
		        } else {
		            builder.Append(this.rootNamespace);
		        }
		        builder.Append("\"");
		        builder.Append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		        builder.Append("    xmlns:").Append(BASE).Append("=\"").Append(BASE_NAMESPACE).Append("\"\n");
		
		        if (!String.IsNullOrEmpty(this.message.ReplyQueue))
		        {
		            builder.Append("    xmlns:").Append(SERVICE).Append("=\"").Append(SERVICE_NAMESPACE).Append("\"\n");
		        }
		
		        foreach (DictionaryEntry entry in this.namespaces) {
		            builder.Append("    xmlns:");
		            builder.Append(entry.Key);
		            builder.Append("=\"");
		            if (!this.withOwnFullNamespaces) {
		                builder.Append(ASB_BASE_NAMESPACE);
		            }
		            builder.Append(entry.Value);
		            builder.Append("\"\n");
		        }
		
		        builder.Append("xsi:schemaLocation=\"");
		        AppendSchemaLocationWithOwnNamespace(builder, BASE_NAMESPACE, BASE_NAMESPACE_LOCATION);
		        if (!String.IsNullOrEmpty(this.message.ReplyQueue))
		        {
		            AppendSchemaLocationWithOwnNamespace(builder, SERVICE_NAMESPACE, BASE_SERVICE_LOCATION);
		        }
		
		        if (String.IsNullOrEmpty(this.rootLocation))
		        {
		            AppendSchemaLocation(builder, this.message.MessageType);
		        } 
		        else
		        {
		            AppendSchemaLocationWithOwnNamespace(builder, this.rootNamespace, this.rootLocation);
		        }
		
		        if (!this.withOwnFullNamespaces)
		        {
		            foreach (DictionaryEntry entry in this.namespaces) 
		            {
		            	AppendSchemaLocation(builder, (string)entry.Value);
		            }
		        }
		        else 
		        {
		            foreach (DictionaryEntry entry in  this.namespaces) {
		        		AppendSchemaLocationWithOwnNamespace(builder,(string) entry.Value,(string) this.namespaceLocations[entry.Key]);
		            }
		        }
		
		        builder.Append("\"");
		    }
		
		    private void AppendSchemaLocationWithOwnNamespace(StringBuilder builder, String nmspace, String namespaceLocation) 
		    {
		        builder.Append("    ").Append(nmspace).Append(" ").Append(namespaceLocation).Append("\n");
		    }
		
		    private void AppendSchemaLocation(StringBuilder builder, String messageType) {
		        String uri = "http://www.ifs.tuwien.ac.at/asb/" + messageType;
		        builder.Append("\n        ");
		        builder.Append(uri);
		        builder.Append(' ');
		        builder.Append(uri);
		        builder.Append('/');
		        builder.Append(char.ToLower(messageType[0]));
		        for (int i = 1; i < messageType.Length; ++i) {
		        	char ch = messageType[i];
		            if (char.IsUpper(ch)) {
		                builder.Append('-');
		            }
		            builder.Append(char.ToLower(ch));
		        }
		        builder.Append(".xsd");
		    }
	}
}
