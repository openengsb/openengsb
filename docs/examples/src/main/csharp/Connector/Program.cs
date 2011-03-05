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

using System;

// TODO: Provide and explain this example

/*
 * Be careful! This code (should) work, but does not have to be best-practice in any way. TBH
 * it should simply show you how the basics work. For a real application we would suggest to
 * use Spring.net-NMS since it handles the entire caching and other issues which may be a problem
 * for a real application.
 */

/*
 * The connector example shows how you could provide a connector for an OpenEngSB
 * domain and how to connect and use the connector from the OpenEngSB.
 */

namespace Connector
{
	class Program
	{
		public static void Main(string[] args)
		{
			Console.WriteLine("Hello World!");
			Console.Write("Press any key to continue . . . ");
			Console.ReadKey(true);
		}
	}
}
