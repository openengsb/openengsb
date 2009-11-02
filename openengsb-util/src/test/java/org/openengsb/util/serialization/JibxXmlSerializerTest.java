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

package org.openengsb.util.serialization;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import sun.rmi.rmic.iiop.ClassPathLoader;

public class JibxXmlSerializerTest {

    private Serializer serializer;

    @Before
    public void setUp() {
        serializer = new JibxXmlSerializer();
    }

    @Test
    public void serializeShouldSucceedWithValidInput() throws SerializationException, IOException, URISyntaxException {
        String validFileContent = FileUtils.readFileToString(new File(ClassPathLoader.getSystemResource(
                "serialization/valid.xml").toURI()));
        StringWriter writer = new StringWriter();

        serializer.serialize(new SerializationTestClass("1", 2, 3.3), writer);

        Assert.assertEquals(validFileContent, writer.toString());
    }

    @Test
    public void serializeShouldThrowArgumentExceptionWithNullObject() {
    }

    @Test
    public void serializeShouldThrowArgumentExceptionWithNullWriter() {

    }

    @Test
    public void deserializeShouldSucceedWithValidInput() throws SerializationException, IOException, URISyntaxException {
        String validFileContent = FileUtils.readFileToString(new File(ClassPathLoader.getSystemResource(
                "serialization/valid.xml").toURI()));
        StringReader reader = new StringReader(validFileContent);

        SerializationTestClass deserializedObject = serializer.deserialize(SerializationTestClass.class, reader);

        Assert.assertEquals("1", deserializedObject.getProp1());
        Assert.assertEquals(2, deserializedObject.getProp2());
        Assert.assertEquals(3.3, deserializedObject.getProp3());
    }

    @Test
    public void deserializeShouldThrowArgumentExceptionWithNullClass() {

    }

    @Test
    public void deserializeShouldThrowArgumentExceptionWithNullReader() {

    }

    /*
     * 
     * public void testUnmarshalFinishesSuccessfullyWithValidInput() throws
     * FileNotFoundException, MarshallingException, URISyntaxException {
     * Marshaller m = new JibxXmlMarshaller();
     * 
     * Person p = m.unmarshal(Person.class, new FileReader(new
     * File(ClassLoader.getSystemResource("validPerson.xml") .toURI())));
     * 
     * assertEquals("David", p.getFirstname()); assertEquals("Waldhans",
     * p.getLastname()); assertEquals(27, p.getAge());
     * assertEquals("Wassergasse 10/14", p.getAddress().getStreet());
     * assertEquals(1030, p.getAddress().getZipCode()); assertEquals("Wien",
     * p.getAddress().getCity()); }
     * 
     * public void testUnmarshalThrowsExceptionWithInvalidXmlInput() throws
     * FileNotFoundException, MarshallingException, URISyntaxException {
     * Marshaller m = new JibxXmlMarshaller();
     * 
     * Person p = m.unmarshal(Person.class, new FileReader(new
     * File(ClassLoader.getSystemResource("invalidPerson.xml") .toURI()))); }
     */
}
