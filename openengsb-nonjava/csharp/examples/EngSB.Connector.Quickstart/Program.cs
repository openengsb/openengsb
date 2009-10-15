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
using Spring.Context.Support;
using System;
#endregion

namespace EngSB.Connector.Quickstart
{
	class Program
	{
		public static void Main(string[] args)
		{
			Console.WriteLine("JMS Presentation Application!");
			
			ClientHandler sender = ContextRegistry.GetContext()["ClientHandler"] as ClientHandler;
			
			sender.Send();
			
			Console.WriteLine("Press Enter to shutdown application...");
			
			System.Console.ReadKey();
			
			Console.WriteLine("Shutting down application...");
		}
	}
}
