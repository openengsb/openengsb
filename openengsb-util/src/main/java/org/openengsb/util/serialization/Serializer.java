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

/**
 * Generic marshaller interface for all marshalling tasks in the openengsb
 * 
 */
public interface Serializer {
    /**
     * Deserializes an object of the type defined by clazz from the given reader
     * and returns an object of that type.
     * 
     * @param <T> Type of object to be deserialized
     * @param clazz
     * @param reader Source from which the object to be deserialized will be
     *        read
     * @return Deserialized object of type T
     * @throws MarshallingException
     */
    <T> T deserialize(Class<T> clazz, Reader reader) throws SerializationException;

    /**
     * Serializes the given object and writes it to the given writer.
     * 
     * @param <T>
     * @param object Object to be serialized
     * @param writer Destination to which the serialized object will be written
     * @throws MarshallingException
     */
    <T> void serialize(T object, Writer writer) throws SerializationException;
}
