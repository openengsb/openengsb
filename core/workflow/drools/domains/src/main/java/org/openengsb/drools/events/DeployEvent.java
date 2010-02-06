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
package org.openengsb.drools.events;

import org.openengsb.core.model.Event;

public class DeployEvent extends Event {

    public DeployEvent() {
        super("deploy", "deployEvent");
    }

    public void setDeployOutput(String deployOutput) {
        super.setValue("deployOutput", deployOutput);
    }

    public void setDeploySuccessful(boolean deployOutcome) {
        super.setValue("deployOutcome", deployOutcome);
    }

    public String getDeployOutput() {
        return (String) super.getValue("deployOutput");
    }

    public boolean isDeploySuccessful() {
        Boolean value = (Boolean) super.getValue("deployOutcome");
        if (value == null) {
            return false;
        }
        return value.booleanValue();
    }
}
