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

namespace Connector
{
	/// <summary>
	/// Represents the Alive State of the OpenEngSB. This object is used as response object in case of the
	/// getAliveState method of each domain.
	/// </summary>
	public enum AliveState
	{
		/**
		 * domain is connecting
		 */
		CONNECTING,

		/**
		 * domain is online, means it is connected and working
		 */
		ONLINE,

		/**
		 * domain is offline, means an error occurred and it has to be updated
		 */
		OFFLINE,

		/**
		 * domain is disconnected means, from the view point of a domain everything is ok, but there is no connection the
		 * the service( e.g. no internet connection,),
		 */
		DISCONNECTED

	}
}
