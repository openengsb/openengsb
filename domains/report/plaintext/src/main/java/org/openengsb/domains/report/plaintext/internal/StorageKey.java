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

package org.openengsb.domains.report.plaintext.internal;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.domains.report.IdType;

public class StorageKey {
    private final String reportId;
    private final IdType type;
    private final String id;

    public StorageKey(String reportId, IdType type, String id) {
        this.reportId = reportId;
        this.type = type;
        this.id = id;
    }

    public String getReportId() {
        return reportId;
    }

    public IdType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((reportId == null) ? 0 : reportId.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StorageKey)) {
            return false;
        }
        StorageKey other = (StorageKey) obj;
        return ObjectUtils.equals(reportId, other.reportId) && ObjectUtils.equals(id, other.id)
                && ObjectUtils.equals(type, other.type);
    }

    @Override
    public String toString() {
        return "StorageKey [reportId=" + reportId + ", id=" + id + ", type=" + type + "]";
    }

}
