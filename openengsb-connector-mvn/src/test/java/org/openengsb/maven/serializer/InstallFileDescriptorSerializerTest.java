package org.openengsb.maven.serializer;

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
        InstallFileDescriptor descriptor = new InstallFileDescriptor(filePath, groupId, artifactId, version, packaging);

        Source source = InstallFileDescriptorSerializer.serialize(descriptor);

        Assert.assertNotNull(source);

        SourceTransformer transformer = new SourceTransformer();
        String deserializedMessage = transformer.toString(source);

        Assert.assertEquals(String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + validMessageTemplate,
                filePath, groupId, artifactId, version, packaging), deserializedMessage);
    }

    @Test(expected = SerializationException.class)
    public void serializeInvalidDescriptorShouldThrowSerializationException() throws SerializationException {
        InstallFileDescriptor descriptor = new InstallFileDescriptor(null, groupId, artifactId, version, packaging);

        Source source = InstallFileDescriptorSerializer.serialize(descriptor);
    }

    @Test
    public void deserializeValidSourceShouldSucceed() throws SerializationException {
        Source validSource = new StringSource(String.format(validMessageTemplate, filePath, groupId, artifactId,
                version, packaging));

        InstallFileDescriptor descriptor = InstallFileDescriptorSerializer.deserialize(validSource);

        Assert.assertNotNull(descriptor);
        Assert.assertEquals(filePath, descriptor.getFilePath());
        Assert.assertEquals(groupId, descriptor.getGroupId());
        Assert.assertEquals(artifactId, descriptor.getArtifactId());
        Assert.assertEquals(version, descriptor.getVersion());
        Assert.assertEquals(packaging, descriptor.getPackaging());
    }

    @Test(expected = SerializationException.class)
    public void deserializeInvalidSourceShouldThrowSerializationException() throws SerializationException {
        Source invalidSource = new StringSource(invalidMessageTemplate);

        InstallFileDescriptor descriptor = InstallFileDescriptorSerializer.deserialize(invalidSource);
    }

}
