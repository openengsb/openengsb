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

package org.openengsb.maven.common.serializer;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.camel.converter.jaxp.StringSource;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openengsb.maven.common.exceptions.SerializationException;
import org.openengsb.maven.common.pojos.InstallFileDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Serializes the message input to a useful object.
 */
public class InstallFileDescriptorSerializer extends AbstractSerializer {

    public static Source serialize(InstallFileDescriptor descriptor) throws SerializationException {
        if (!descriptor.validate()) {
            throw new SerializationException("The given file descriptor is not valid.");
        }

        String messageTemplate = "<mavenFileInstaller fileToInstall=\"%s\" groupId=\"%s\" artifactId=\"%s\" version=\"%s\" packaging=\"%s\"/>";
        return new StringSource(String.format(messageTemplate, descriptor.getFilePath(), descriptor.getGroupId(),
                descriptor.getArtifactId(), descriptor.getVersion(), descriptor.getPackaging()));
    }

    public static InstallFileDescriptor deserialize(Source src) throws SerializationException {
        InstallFileDescriptor result = new InstallFileDescriptor();

        SourceTransformer transformer = new SourceTransformer();

        try {
            Document doc = transformer.toDOMDocument(src);
            NamedNodeMap attributeMap = doc.getFirstChild().getAttributes();
            Node fileToInstallAttribute = attributeMap.getNamedItem("fileToInstall");
            Node groupIdAttribute = attributeMap.getNamedItem("groupId");
            Node artifactIdAttribute = attributeMap.getNamedItem("artifactId");
            Node versionAttribute = attributeMap.getNamedItem("version");
            Node packagingAttribute = attributeMap.getNamedItem("packaging");

            if (fileToInstallAttribute == null || groupIdAttribute == null || artifactIdAttribute == null
                    || versionAttribute == null || packagingAttribute == null) {
                throw new SerializationException("Not all expected attributes could be found.");
            }

            result.setFilePath(fileToInstallAttribute.getTextContent());
            result.setGroupId(groupIdAttribute.getTextContent());
            result.setArtifactId(artifactIdAttribute.getTextContent());
            result.setVersion(versionAttribute.getTextContent());
            result.setPackaging(packagingAttribute.getTextContent());

        } catch (TransformerException e) {
            throw new SerializationException(e);
        } catch (ParserConfigurationException e) {
            throw new SerializationException(e);
        } catch (IOException e) {
            throw new SerializationException(e);
        } catch (SAXException e) {
            throw new SerializationException(e);
        }

        return result;
    }
}
