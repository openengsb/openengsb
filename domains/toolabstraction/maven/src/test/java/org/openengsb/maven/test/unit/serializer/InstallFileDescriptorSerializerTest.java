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

package org.openengsb.maven.test.unit.serializer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.common.exceptions.SerializationException;
import org.openengsb.maven.common.pojos.InstallFileDescriptor;
import org.openengsb.maven.common.serializer.InstallFileDescriptorSerializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class InstallFileDescriptorSerializerTest extends TestCase {

    final String validMessageTemplate = "<mavenFileInstaller fileToInstall=\"%s\" groupId=\"%s\" artifactId=\"%s\" version=\"%s\" packaging=\"%s\"/>";
    final String invalidMessageTemplate = "<mavenFileInstallerInvalid fileToInstallInvalid=\"\" groupIdInvalid=\"\" artifactIdInvalid=\"\" versionInvalid=\"\" packagingInvalid=\"\"/>";

    final String filePath = "myfilepath";
    final String groupId = "mygroupId";
    final String artifactId = "myartifactId";
    final String version = "myversion1.0";
    final String packaging = "jar";

    @Test
    public void serializeValidDescriptorShouldSucceed() throws TransformerException, SerializationException {
        InstallFileDescriptor descriptor = new InstallFileDescriptor(this.filePath, this.groupId, this.artifactId,
                this.version, this.packaging);

        Source source = InstallFileDescriptorSerializer.serialize(descriptor);

        Assert.assertNotNull(source);

        SourceTransformer transformer = new SourceTransformer();
        String deserializedMessage = transformer.toString(source);

        Assert.assertEquals(String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + this.validMessageTemplate,
                this.filePath, this.groupId, this.artifactId, this.version, this.packaging), deserializedMessage);
    }

    @Test(expected = SerializationException.class)
    public void serializeInvalidDescriptorShouldThrowSerializationException() throws SerializationException {
        InstallFileDescriptor descriptor = new InstallFileDescriptor(null, this.groupId, this.artifactId, this.version,
                this.packaging);
        InstallFileDescriptorSerializer.serialize(descriptor);
    }

    @Test
    public void deserializeValidSourceShouldSucceed() throws SerializationException {
        Source validSource = new StringSource(String.format(this.validMessageTemplate, this.filePath, this.groupId,
                this.artifactId,
                this.version, this.packaging));

        InstallFileDescriptor descriptor = InstallFileDescriptorSerializer.deserialize(validSource);

        Assert.assertNotNull(descriptor);
        Assert.assertEquals(this.filePath, descriptor.getFilePath());
        Assert.assertEquals(this.groupId, descriptor.getGroupId());
        Assert.assertEquals(this.artifactId, descriptor.getArtifactId());
        Assert.assertEquals(this.version, descriptor.getVersion());
        Assert.assertEquals(this.packaging, descriptor.getPackaging());
    }

    @Test(expected = SerializationException.class)
    public void deserializeInvalidSourceShouldThrowSerializationException() throws SerializationException {
        Source invalidSource = new StringSource(this.invalidMessageTemplate);

        InstallFileDescriptorSerializer.deserialize(invalidSource);
    }

}
