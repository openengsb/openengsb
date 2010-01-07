/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.edb.jbi.endpoints;

import org.openengsb.edb.core.api.EDBHandlerFactory;

public class EDBEndPointConfig {
	private EDBHandlerFactory factory;

	private String linkStorage;

	public String getLinkStorage() {
		return linkStorage;
	}

	public void setLinkStorage(String linkStorage) {
		this.linkStorage = linkStorage;
	}

	public EDBHandlerFactory getFactory() {
		return factory;
	}

	public void setFactory(EDBHandlerFactory factory) {
		this.factory = factory;
	}

}
