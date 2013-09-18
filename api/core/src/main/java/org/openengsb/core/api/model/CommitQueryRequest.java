/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.api.model;

import com.google.common.base.Objects;

/**
 * This class is a container to define the conditions for the query of commit objects from the EDB. All elements which
 * are null are ignored during the querying process. The only exceptions are the beginTimestamp and the endTimestamp.
 * For the beginTimestamp the default value 0 is taken and for the endTimestamp the default value Long.MAX_VALUE is
 * taken. In this way, these both values can be used in a "between" clause in the SQL command.
 */
public class CommitQueryRequest {
    private String context;
    private String committer;
    private Long startTimestamp;
    private Long endTimestamp;

    public CommitQueryRequest() {
        startTimestamp = 0L;
        endTimestamp = Long.MAX_VALUE;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass())
            .add("context", context)
            .add("committer", committer)
            .add("startTimestamp", startTimestamp)
            .add("endTimestamp", endTimestamp)
            .omitNullValues()
            .toString();
    }
}
