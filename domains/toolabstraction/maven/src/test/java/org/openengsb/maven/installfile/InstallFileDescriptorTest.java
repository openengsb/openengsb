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

package org.openengsb.maven.installfile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.maven.common.pojos.InstallFileDescriptor;

public class InstallFileDescriptorTest {

    @Test
    @Ignore
    public void validate_shouldReturnTrueIfAllDescriptorAttributesAreSet() {
        InstallFileDescriptor descriptor = new InstallFileDescriptor();
        descriptor.setArtifactId("myArtifactId");
        descriptor.setFilePath("myFilePath");
        descriptor.setGroupId("myGroupId");
        descriptor.setPackaging("jar");
        descriptor.setVersion("myVersion");

        assertTrue(descriptor.validate());
    }

    @Test
    @Ignore
    public void validate_shouldReturnFalseIfAnyAttributeIsEmptyOrNull() {
        InstallFileDescriptor d1Empty = new InstallFileDescriptor("", "myGroupId", "myArtifactId", "myVersion", "jar");
        InstallFileDescriptor d2Empty = new InstallFileDescriptor("myFilePath", "", "myArtifactId", "myVersion", "jar");
        InstallFileDescriptor d3Empty = new InstallFileDescriptor("myFilePath", "myGroupId", "", "myVersion", "jar");
        InstallFileDescriptor d4Empty = new InstallFileDescriptor("myFilePath", "myGroupId", "myArtifactId", "", "jar");
        InstallFileDescriptor d5Empty = new InstallFileDescriptor("myFilePath", "myGroupId", "myArtifactId",
                "myVersion", "");

        InstallFileDescriptor d1Null = new InstallFileDescriptor(null, "myGroupId", "myArtifactId", "myVersion", "jar");
        InstallFileDescriptor d2Null = new InstallFileDescriptor("myFilePath", null, "myArtifactId", "myVersion", "jar");
        InstallFileDescriptor d3Null = new InstallFileDescriptor("myFilePath", "myGroupId", null, "myVersion", "jar");
        InstallFileDescriptor d4Null = new InstallFileDescriptor("myFilePath", "myGroupId", "myArtifactId", null, "jar");
        InstallFileDescriptor d5Null = new InstallFileDescriptor("myFilePath", "myGroupId", "myArtifactId",
                "myVersion", null);

        assertFalse(d1Empty.validate());
        assertFalse(d2Empty.validate());
        assertFalse(d3Empty.validate());
        assertFalse(d4Empty.validate());
        assertFalse(d5Empty.validate());

        assertFalse(d1Null.validate());
        assertFalse(d2Null.validate());
        assertFalse(d3Null.validate());
        assertFalse(d4Null.validate());
        assertFalse(d5Null.validate());
    }

    @Test
    @Ignore
    public void validate_shouldReturnFalseIfPackagingIsSetToAnInvalidValue() {
        InstallFileDescriptor invalidPackagingDescriptor = new InstallFileDescriptor("myFilePath", "myGroupId",
                "myArtifactId", "myVersion", "invalidpackaging");

        assertFalse(invalidPackagingDescriptor.validate());
    }
}
