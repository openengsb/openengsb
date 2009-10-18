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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.config.jbi.ParseException;
import org.xml.sax.InputSource;

public class ComponentParserTest
{
	private final DocumentBuilderFactory dbf;
	private final DocumentBuilder db;
	private ComponentParser parser;

	public ComponentParserTest() throws ParserConfigurationException {
		dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();
	}

	@Before
	public void setup() {
		parser = new ComponentParser();
	}

	private static InputSource getFile(String name) {
		return new InputSource(ClassLoader.getSystemResourceAsStream(name));
	}

	@Test(expected = ParseException.class)
	public void parseJbi_nonComponentDescriptor_shouldThrowException() throws Exception {
		parser.parseJbi(new InputSource(new StringReader(
		"<jbi><services binding-component=\"false\"/></jbi>")));
	}

	@Test
	public void parseJbi_validDescriptorWithAttributes_shouldReadAttributesCorrect()
			throws Exception {
		ComponentDescriptor desc = parser.parseJbi(getFile("descriptors/simple.xml"));
		assertThat(desc.getName(), is("thename"));
		assertThat(desc.getType(), is(ComponentDescriptor.Type.BINDING_COMPONENT));
		assertThat(desc.getDescription(), is("thedesc"));
	}

	@Test
	public void parseSchema_minimumSchema_shouldReadTargetNamespaceCorrect() throws Exception {
		ComponentDescriptor desc = parser.parseSchema(getFile("descriptors/simple.xsd"));
		assertThat(desc.getTargetNamespace(), is("http://openengsb.org/simple"));
	}

	@Test
	public void parseSchema_minimumSchema_shouldReadEndpointNameCorrect() throws Exception {
		ComponentDescriptor desc = parser.parseSchema(getFile("descriptors/simple.xsd"));
		assertThat(desc.getEndpoints().size(), is(1));
		assertThat(desc.getEndpoints().get(0).getName(), is("theEndpoint"));
	}

	@Test
	public void parseSchema_minimumSchema_shouldReadAttributesCorrect() throws Exception {
		ArrayList<AttributeDescriptor> attrs = parser.parseSchema(
				getFile("descriptors/simple.xsd")).getEndpoints().get(0).getAttributes();
		assertThat(attrs.size(), is(3));
		assertThat(attrs.get(0).getName(), is("a"));
		assertThat(attrs.get(0).getType(), is(AttributeDescriptor.Type.STRING));
		assertThat(attrs.get(1).getName(), is("b"));
		assertThat(attrs.get(1).getType(), is(AttributeDescriptor.Type.BOOLEAN));
		assertThat(attrs.get(2).getName(), is("c"));
		assertThat(attrs.get(2).getType(), is(AttributeDescriptor.Type.QNAME));
	}
}
