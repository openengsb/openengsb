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
package org.openengsb.ekb.resources;

public class OpenEngSBMessageSegment {

    private String segmentName;
    private String domainConcept;
    private String format;
    private String content;

    public OpenEngSBMessageSegment(String name, String domainConcept, String format, String content) {
        this.segmentName = name;
        this.domainConcept = domainConcept;
        this.format = format;
        this.content = content;
    }

    public String getSegmentName() {
        return this.segmentName;
    }

    public String getDomainConcept() {
        return this.domainConcept;
    }

    public String getFormat() {
        return this.format;
    }

    public String getContent() {
        return this.content;
    }
}