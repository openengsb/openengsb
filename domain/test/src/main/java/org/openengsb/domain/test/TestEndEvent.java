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

package org.openengsb.domain.test;

import org.openengsb.core.common.Event;

public class TestEndEvent extends Event {

    private String testId;
    private boolean success;
    private String output;

    public TestEndEvent(String testId, boolean success, String output) {
        super("TestEndEvent");
        this.testId = testId;
        this.success = success;
        this.output = output;
    }

    public String getTestId() {
        return testId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }

}
