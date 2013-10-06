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
package org.openengsb.core.edb.jpa.internal;

import javax.persistence.Column;
import javax.persistence.Entity;
import org.openengsb.core.edb.api.EDBStage;
import org.apache.openjpa.persistence.jdbc.Index;

@SuppressWarnings("serial")
@Entity
public class JPAStage extends VersionedEntity implements EDBStage {
	@Column(name="STAGEID")
	@Index
	private String stageId;
	@Column(name="CREATOR")
	private String creator;
	@Column(name="TIMESTAMP")
	private Long timeStamp;
	
	@Override
	public void setStageId(String id) {
		this.stageId = id;
	}

	@Override
	public String getStageId() {
		return this.stageId;
	}

	@Override
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public String getCreator() {
		return this.creator;
	}

	@Override
	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public Long getTimeStamp() {
		return this.timeStamp;
	}
}
