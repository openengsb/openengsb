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
using System.Web.Script.Serialization;
using Apache.NMS;

/*
 * Be careful! This code (should) work, but does not have to be best-practice in any way. TBH
 * it should simply show you how the basics work. For a real application we would suggest to
 * use Spring.net-NMS since it handles the entire caching and other issues which may be a problem
 * for a real application.
 */

/*
 * This example represents how a connector for the OpenEngSB have to be integrated. Basically the entire
 * communication is based on JSON and the Request and Response objects. You're listening to a queue
 * which you define on the OpenEngSB as proxy. First of all, all messages received on a queue have to
 * be converted into Request objects. Then define what classes/methods you want to response to how; build the
 * Response objects and send them to an Queue with the ID on the same host the Request is comming from.
 */

namespace Connector
{
	class Program
	{
		public static void Main(string[] args)
		{
			// Example connection strings:
			//    activemq:tcp://activemqhost:61616
			//    stomp:tcp://activemqhost:61613
			//    ems:tcp://tibcohost:7222
			//    msmq://localhost

			Uri connecturi = new Uri("activemq:tcp://localhost:6549");
			
			Console.WriteLine("About to connect to " + connecturi);

			// NOTE: ensure the nmsprovider-activemq.config file exists in the executable folder.
			IConnectionFactory factory = new NMSConnectionFactory(connecturi);

			using (IConnection connection = factory.CreateConnection())
				using (ISession session = connection.CreateSession())
			{
				// Examples for getting a destination:
				//
				// Hard coded destinations:
				//    IDestination destination = session.GetQueue("FOO.BAR");
				//    Debug.Assert(destination is IQueue);
				//    IDestination destination = session.GetTopic("FOO.BAR");
				//    Debug.Assert(destination is ITopic);
				//
				// Embedded destination type in the name:
				//    IDestination destination = SessionUtil.GetDestination(session, "queue://FOO.BAR");
				//    Debug.Assert(destination is IQueue);
				//    IDestination destination = SessionUtil.GetDestination(session, "topic://FOO.BAR");
				//    Debug.Assert(destination is ITopic);
				//
				// Defaults to queue if type is not specified:
				//    IDestination destination = SessionUtil.GetDestination(session, "FOO.BAR");
				//    Debug.Assert(destination is IQueue);
				//
				// .NET 3.5 Supports Extension methods for a simplified syntax:
				//    IDestination destination = session.GetDestination("queue://FOO.BAR");
				//    Debug.Assert(destination is IQueue);
				//    IDestination destination = session.GetDestination("topic://FOO.BAR");
				//    Debug.Assert(destination is ITopic);

				// This queue have to be specified on the server during the creation of the proxy
				IDestination destination = session.GetDestination("queue://FOO.BAR");
				Console.WriteLine("Using incomming destination: " + destination);

				// Create a consumer and producer
				
				using (IMessageConsumer consumer = session.CreateConsumer(destination))
				{
					// Start the connection so that messages will be processed.
					connection.Start();

					// Consume a message
					ITextMessage message = consumer.Receive(new TimeSpan(1,0,0)) as ITextMessage;
					if (message == null)
					{
						Console.WriteLine("No message received!");
					}
					else
					{
						Console.WriteLine("Received message with ID:   " + message.NMSMessageId);
						Console.WriteLine("Received message with text: " + message.Text);
						JavaScriptSerializer ser = new JavaScriptSerializer();
						Request requestMapping = (Request) ser.Deserialize(message.Text, typeof(Request));
						if(requestMapping.methodName == "getAliveState")
						{
							IDestination outgoingDestination = session.GetDestination("queue://"+requestMapping.callId);
							Console.WriteLine("Using outgoing destination: " + outgoingDestination);
							using (IMessageProducer producer = session.CreateProducer(outgoingDestination))
							{
								Response responseMapping = ProduceAnswerMapping(ser, requestMapping);
								String answer = ser.Serialize(responseMapping);
								Console.WriteLine("Created answer text: " + answer);
								// Send a message
								producer.DeliveryMode = MsgDeliveryMode.Persistent;
								ITextMessage request = session.CreateTextMessage(answer);
								producer.Send(request);
							}
						}
						else
						{
							Console.WriteLine("Unknown method; ignore...");
						}
					}
				}
				Console.Write("Press any key to continue . . . ");
				Console.ReadKey(true);
			}
		}

		static Response ProduceAnswerMapping(JavaScriptSerializer ser, Request requestMapping)
		{
			Response responseMapping = new Response();
			responseMapping.type = Response.ReturnType.Object;
			responseMapping.callId = requestMapping.callId;
			responseMapping.metaData = requestMapping.metaData;
			responseMapping.className = "org.openengsb.core.api.AliveState";
			responseMapping.arg = AliveState.ONLINE;
			return responseMapping;
		}
	}
}
