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
using Apache.NMS;

/*
 * Be careful! This code (should) work, but does not have to be best-practice in any way. TBH
 * it should simply show you how the basics work. For a real application we would suggest to
 * use Spring.net-NMS since it handles the entire caching and other issues which may be a problem
 * for a real application.
 */

/*
 * This example has nothing to do with the OpenEngSB directly but rather simply shows 
 * how to communicate via NMS. You need to start AMQ(http://activemq.apache.org/download.html)
 * first. If you do not change any ports and run AMQ on the same computer as this example you do
 * not have to change anything else. The sources for this example are copied from
 * 
 *  https://cwiki.apache.org/NMS/nms-simple-synchornous-consumer-example.html
 * 
 */

namespace Simple
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

			Uri connecturi = new Uri("activemq:tcp://localhost:61616");
			
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

				IDestination destination = session.GetDestination("queue://FOO.BAR");
				Console.WriteLine("Using destination: " + destination);

				// Create a consumer and producer
				using (IMessageConsumer consumer = session.CreateConsumer(destination))
					using (IMessageProducer producer = session.CreateProducer(destination))
				{
					// Start the connection so that messages will be processed.
					connection.Start();
					producer.DeliveryMode = MsgDeliveryMode.Persistent;
					
					// Send a message
					ITextMessage request = session.CreateTextMessage("Hello World!");
					request.NMSCorrelationID = "abc";
					request.Properties["NMSXGroupID"] = "cheese";
					request.Properties["myHeader"] = "Cheddar";

					producer.Send(request);

					// Consume a message
					ITextMessage message = consumer.Receive() as ITextMessage;
					if(message == null)
					{
						Console.WriteLine("No message received!");
					}
					else
					{
						Console.WriteLine("Received message with ID:   " + message.NMSMessageId);
						Console.WriteLine("Received message with text: " + message.Text);
					}
				}
			}
			Console.Write("Press any key to continue . . . ");
			Console.ReadKey(true);
		}
	}
}
