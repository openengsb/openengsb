/**

   Copyright 2009 EngSB Team QSE/IFS, Vienna University of Technology

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

package org.openengsb.maven.se.endpoints;

import java.io.File;
import java.util.Properties;

import org.apache.maven.wagon.PathUtils;
import org.openengsb.maven.common.domains.InstallFileDomain;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.InstallFileDescriptor;
import org.openengsb.maven.common.pojos.Options;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.se.AbstractMavenEndpoint;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Endpoint implementing maven's install:install-file functionality.
 */
/**
 * @org.apache.xbean.XBean element="mavenFileInstaller"
 */
public class MavenInstallFileEndpoint extends AbstractMavenEndpoint implements
		InstallFileDomain {

	@Override
	public MavenResult installFile(InstallFileDescriptor fileDescriptor)
			throws MavenException {
		if (!fileDescriptor.validate()) {
			MavenResult result = new MavenResult();
			result.setMavenOutput(MavenResult.ERROR);
			result.setErrorMessage("Given file descriptor is invalid.");
			return result;
		}

		// extract file name and path to file from file descriptor
		String fileName = PathUtils.filename(fileDescriptor.getFilePath());
		String pathToFile = PathUtils.dirname(fileDescriptor.getFilePath());

		Properties props = new Properties();
		props.setProperty("file", fileName);
		props.setProperty("groupId", fileDescriptor.getGroupId());
		props.setProperty("artifactId", fileDescriptor.getArtifactId());
		props.setProperty("version", fileDescriptor.getVersion());
		props.setProperty("packaging", fileDescriptor.getPackaging());

		this.projectConfiguration.setBaseDirectory(new File(pathToFile));

		return execute(pathToFile, Arrays
				.asList(new String[] { "install:install-file" }), props);
	}

	public void setOptions(Options options) {
		this.options = options;
	}
}
