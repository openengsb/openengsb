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

package org.openengsb.core.messaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListSegment extends Segment {

	private final List<Segment> list;

	public ListSegment(Builder builder) {
		super(builder.name, builder.format, builder.domainConcept);
		this.list = Collections.unmodifiableList(builder.list);
	}

	private ListSegment() {
		// for jibx
		super("", "", "");
		this.list = listFactory();
	}

	private static List<Segment> listFactory() {
		// for jibx
		return new ArrayList<Segment>();
	}

	public List<Segment> getList() {
		return list;
	}

	@Override
	public String toString() {
		return String.format(
				"[ListSegment name=%s, format=%s, domainConcept=%s, list=%s]",
				getName(), getFormat(), getDomainConcept(), list);
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash * 31 + list.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ListSegment)) {
			return false;
		}

		ListSegment other = (ListSegment) obj;

		return super.equals(other) && list.equals(other.list);
	}

	public static class Builder {
		private String name;
		private String format = "";
		private String domainConcept = "";
		private List<Segment> list;

		public ListSegment build() {
			validate();
			return new ListSegment(this);
		}

		private void validate() {
			if (list == null) {
				throw new IllegalStateException("List must not be null");
			}
		}

		public Builder list(List<Segment> list) {
			this.list = list;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder format(String format) {
			this.format = format;
			return this;
		}

		public Builder domainConcept(String domainConcept) {
			this.domainConcept = domainConcept;
			return this;
		}
	}
}
