/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.config.jbi.component;

import java.util.ArrayList;

public class ComponentDescriptor
{
	public static enum Type {
		BINDING_COMPONENT("binding-component"), SERVICE_ENGINE("service-engine");

		private String textual;

		Type(String textual) {
			this.textual = textual;
		}

		public String getTextual() {
			return textual;
		}
	}

	private final Type type;
	private final String name;
	private final String description;
	private final String targetNamespace;
	private final ArrayList<EndpointDescriptor> endpoints;

	ComponentDescriptor(Type type, String name, String description) {
		this(type, name, description, null, null);
	}

	ComponentDescriptor(String targetNamespace, ArrayList<EndpointDescriptor> endpoints) {
		this(null, null, null, targetNamespace, endpoints);
	}

	public ComponentDescriptor(Type type, String name, String description, String targetNamespace,
			ArrayList<EndpointDescriptor> endpoints) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.targetNamespace = targetNamespace;
		this.endpoints = endpoints;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getTargetNamespace() {
		return targetNamespace;
	}

	public ArrayList<EndpointDescriptor> getEndpoints() {
		return endpoints;
	}
}
