/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.report;

import java.util.Collections;
import java.util.List;

import com.sun.xml.internal.ws.api.message.Attachment;

public class Report {

    private byte[] content;

    private String contentType;

    private String name;

    private List<Attachment> attachments;

    @SuppressWarnings("unused")
    private Report() {
        // for rpc framework
    }

    public Report(byte[] content, String contentType, String name) {
        this.content = content;
        this.contentType = contentType;
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }

    public String getName() {
        return name;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public List<Attachment> getAttachments() {
        if (attachments == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(attachments);
    }
}
