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
 * The example shown in the manual, explaining how an Workflow can be called at the OpenEngSB
 * is implemented here in csharp showing how such an implementation could finally look like.
 * For this example two conditions are important:
 * 
 * 1) The OpenEngSB have to be up and running
 * 2) The default port of the JMS OpenEngSB Connector should be used (otherwise this sample has
 *  to be adapte)
 * 3) the simpleFlow has to be deployed at the OpenEngSB
 */

namespace Workflow
{
	class Program
	{
		public static void Main(string[] args)
		{
			// This ID is important since it will be also the queue a response will be sent.
			string queueId = Guid.NewGuid().ToString();
			// This message which should be send to the server
			string requestMessage = ""
				// general metda-data first; then the real call definition
				+ "{"
				// the call ID; this will also be the queue a response will be sent. Be careful to make it unqiue
               + "    \"callId\": \"" + queueId + "\","
				// if the server should answer or not
               + "    \"answer\": true,"
				// the internal definition of the method call
               + "    \"methodCall\": {"
				// of which type the sent data is; this have to be the required java types
               + "         \"classes\": ["
               + "             \"java.lang.String\","
               + "             \"org.openengsb.core.api.workflow.model.ProcessBag\""
               + "         ],"
				// the method which should be executed
               + "         \"methodName\": \"executeWorkflow\","
				// the "header-data" of the message; this is not the header of JMS to use; eg stomp uses the same one
               + "         \"metaData\": {"
				// the ID of the internal service to be called
				// alternatively a OSGi filter could be defined to call a service. For example instead of the serviceId definition you can use:
				//  \"serviceFilter\": \"(objectClass=org.openengsb.core.api.workflow.WorkflowService)\","
               + "             \"serviceId\": \"workflowService\","
				// the context in which the service should be called
               + "             \"contextId\": \"foo\""
               + "         },"
				// the arguments with which the workflowService method should be called
               + "         \"args\": ["
				// the name of the workflow to be executed
               + "             \"simpleFlow\","
				// the params which should be put into the prcoess bag initially
               + "             {"
               + "             }"
               + "         ]"
               + "     }"
               + "}";
			
			// the OpenEngSB connection URL
			Uri connecturi = new Uri("activemq:tcp://localhost:6549");
			
			Console.WriteLine("About to connect to " + connecturi);

			// NOTE: ensure the nmsprovider-activemq.config file exists in the executable folder.
			IConnectionFactory factory = new NMSConnectionFactory(connecturi);

			using (IConnection connection = factory.CreateConnection())
				using (ISession session = connection.CreateSession())
			{
				IDestination destination = session.GetDestination("receive");
				Console.WriteLine("Using destination for sending: " + destination);

				
				using (IMessageProducer producer = session.CreateProducer(destination))
				{
					connection.Start();
					producer.DeliveryMode = MsgDeliveryMode.Persistent;
					ITextMessage request = session.CreateTextMessage(requestMessage);
					producer.Send(request);
				}
				
				IDestination receiveDest = session.GetDestination(queueId);
				Console.WriteLine("Using destination for receiving: " + receiveDest);
				
				using (IMessageConsumer consumer = session.CreateConsumer(receiveDest)) 
				{
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
