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

public class BuildEvent extends Event {
    public BuildEvent() {
        super("build", "buildEvent");
    }

    public void setBuildOutput(String buildOutput) {
        super.setValue("buildOutput", buildOutput);
    }

    public void setBuildSuccessful(boolean buildOutcome) {
        super.setValue("buildOutcome", buildOutcome);
    }

    public String getBuildOutput() {
        return (String) super.getValue("buildOutput");
    }

    public boolean isBuildSuccessful() {
        Boolean value = (Boolean) super.getValue("buildOutcome");
        if (value == null) {
            return false;
        }
        return value.booleanValue();
    }
}
