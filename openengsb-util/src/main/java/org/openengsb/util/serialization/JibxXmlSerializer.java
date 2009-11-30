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

import java.io.Reader;
import java.io.Writer;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

public class JibxXmlSerializer implements Serializer {

    @Override
    public <T> void serialize(T object, Writer writer) throws SerializationException {

        try {
            IBindingFactory bfact = BindingDirectory.getFactory(object.getClass());

            IMarshallingContext mctx = bfact.createMarshallingContext();
            mctx.marshalDocument(object, "UTF-8", null, writer);
        } catch (JiBXException e) {
            throw new SerializationException(String.format("Error serializing object of type %s.", object.getClass()
                    .getName()), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(Class<T> clazz, Reader reader) throws SerializationException {
        try {
            IBindingFactory bfact = BindingDirectory.getFactory(clazz);

            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            Object obj = uctx.unmarshalDocument(reader, null);
            return (T) obj;
        } catch (JiBXException e) {
            throw new SerializationException("Error deserializing from reader.", e);
        }
    }

}
