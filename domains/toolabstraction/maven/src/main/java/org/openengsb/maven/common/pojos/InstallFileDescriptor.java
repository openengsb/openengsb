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

package org.openengsb.maven.common.pojos;

import org.apache.commons.lang.ArrayUtils;

/**
 * The InstallFileDescriptor describes the properties of a file that should be
 * installed into a maven repository.
 */
public class InstallFileDescriptor {

    private static final String[] VALID_PACKAGING_TYPES = new String[] { "jar" };

    /**
     * The absolute file path of the file to be installed into the repository
     */
    private String filePath;
    private String groupId;
    private String artifactId;
    private String version;
    private String packaging;

    public InstallFileDescriptor() {

    }

    public InstallFileDescriptor(String filePath, String groupId,
            String artifactId, String version, String packaging) {
        this.filePath = filePath;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackaging() {
        return this.packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public boolean validate() {
        return (!isNullOrEmpty(this.filePath) && !isNullOrEmpty(this.groupId)
                && !isNullOrEmpty(this.artifactId) && !isNullOrEmpty(this.version)
                && !isNullOrEmpty(this.packaging) && ArrayUtils.contains(
                VALID_PACKAGING_TYPES, this.packaging));
    }

    private boolean isNullOrEmpty(String s) {
        return (s == null || s.equals(""));
    }
}
