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

package org.openengsb.core.messaging;

public class TextSegment extends Segment {

    private final String text;

    private TextSegment(Builder builder) {
        super(builder.name, builder.format, builder.domainConcept);
        this.text = builder.text;
    }

    private TextSegment() {
        // needed for jibx
        super("", "", "");
        this.text = "";
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return String.format("[TextSegment, name=%s, format=%s, domainConcept=%s, text=%s]", getName(), getFormat(),
                getDomainConcept(), getText());
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = hash * 31 + text.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TextSegment)) {
            return false;
        }

        TextSegment other = (TextSegment) obj;

        return super.equals(other) && text.equals(other.text);
    }

    public static class Builder {
        private String name;
        private String format = "";
        private String domainConcept = "";
        private String text;

        public Builder() {
        }

        public Builder(String name) {
            name(name);
        }

        public TextSegment build() {
            validate();
            return new TextSegment(this);
        }

        private void validate() {
            if (text == null) {
                throw new IllegalStateException("Text must not be null");
            }
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

        public Builder text(String text) {
            this.text = text;
            return this;
        }
    }
}
