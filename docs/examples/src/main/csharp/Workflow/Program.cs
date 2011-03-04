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

using System;
using Apache.NMS;

// TODO: Provide and explain this example

/*
 * The example shown in the manual, explaining how an Workflow can be called at the OpenEngSB
 * is implemented here in csharp showing how such an implementation could finally look like.
 */

namespace Workflow
{
	class Program
	{
		public static void Main(string[] args)
		{
			string queueId = "12345";
			string requestMessage = ""
				+ "{"
				+ "    \"callId\": \"" + queueId + "\","
				+ "    \"answer\": true,"
				+ "    \"classes\": ["
				+ "        \"java.lang.String\","
				+ "        \"org.openengsb.core.common.workflow.model.ProcessBag\""
				+ "    ],"
				+ "    \"methodName\": \"executeWorkflow\","
				+ "    \"metaData\": {"
				+ "        \"serviceId\": \"workflowService\","
				+ "        \"contextId\": \"foo\""
				+ "    },"
				+ "    \"args\": ["
				+ "        \"simpleFlow\","
				+ "        {"
				+ "        }"
				+ "    ]"
				+ "}";
			
			Uri connecturi = new Uri("activemq:tcp://localhost:6549");
			
			Console.WriteLine("About to connect to " + connecturi);

			// NOTE: ensure the nmsprovider-activemq.config file exists in the executable folder.
			IConnectionFactory factory = new NMSConnectionFactory(connecturi);

			using(IConnection connection = factory.CreateConnection())
				using(ISession session = connection.CreateSession())
			{
				IDestination destination = session.GetDestination("receive");
				Console.WriteLine("Using destination for sending: " + destination);

				
				using(IMessageProducer producer = session.CreateProducer(destination))
				{
					connection.Start();
					producer.DeliveryMode = MsgDeliveryMode.Persistent;
					ITextMessage request = session.CreateTextMessage(requestMessage);
					producer.Send(request);
				}
				
				IDestination receiveDest = session.GetDestination(queueId);
				Console.WriteLine("Using destination for receiving: " + receiveDest);
				
				using(IMessageConsumer consumer = session.CreateConsumer(receiveDest)) {
					ITextMessage message = consumer.Receive() as ITextMessage;
					if(message == null)
					{
						Console.WriteLine("No message received!");
					}
					else
					{
						Console.WriteLine("Received message with text: " + message.Text);
					}
				}
			}
			
			Console.Write("Press any key to continue . . . ");
			Console.ReadKey(true);
		}
	}
}