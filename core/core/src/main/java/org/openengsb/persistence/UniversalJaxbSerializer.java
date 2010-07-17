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
package org.openengsb.persistence;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class UniversalJaxbSerializer {

    public String serialize(Object o) throws JAXBException {
        Class<?> objectClass = o.getClass();
        JAXBContext context = JAXBContext.newInstance(objectClass);

        Marshaller m = context.createMarshaller();
        StringWriter resultWriter = new StringWriter();
        StreamResult result = new StreamResult(resultWriter);

        @SuppressWarnings("unchecked")
        JAXBElement<?> jaxbElement = new JAXBElement(new QName(objectClass.getSimpleName()), objectClass, o);

        m.marshal(jaxbElement, result);

        return resultWriter.toString();
    }

    public <T> T deserialize(Class<T> clazz, String doc) throws JAXBException {
        Source source = new StreamSource(new StringReader(doc));
        JAXBContext context = JAXBContext.newInstance(clazz);
        Unmarshaller u = context.createUnmarshaller();
        return u.unmarshal(source, clazz).getValue();
    }
}
