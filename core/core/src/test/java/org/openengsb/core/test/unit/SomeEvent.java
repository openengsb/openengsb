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
package org.openengsb.core.test.unit;

import org.openengsb.core.model.Event;

public class SomeEvent extends Event {

    public SomeEvent() {
        super("testDomain", "someTestEvent");
        super.setToolConnector("testTool");
    }

    public void setBean(Bean b) {
        super.setValue("bean", b);
    }

    public Bean getBean() {
        return (Bean) super.getValue("bean");
    }

    public static class Bean {
        private String a;
        private Bean b;

        public Bean() {
        }

        public Bean(String a, Bean b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((a == null) ? 0 : a.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Bean other = (Bean) obj;
            if (a == null) {
                if (other.a != null)
                    return false;
            } else if (!a.equals(other.a))
                return false;
            return true;
        }

        public void setBean(Bean b) {
            this.b = b;
        }

        public Bean getBean() {
            return b;
        }

    }
}
