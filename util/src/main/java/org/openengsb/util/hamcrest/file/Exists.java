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
package org.openengsb.util.hamcrest.file;

import java.io.File;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class Exists extends TypeSafeMatcher<File> {
    private File fileTested;

    @Override
    public boolean matchesSafely(File file) {
        this.fileTested = file;
        return this.fileTested.exists();
    }

    @Override
    public void describeTo(Description desc) {
        desc.appendText(" that file ");
        desc.appendValue(this.fileTested);
        desc.appendText(" exists");
    }

    public static Matcher<File> exists() {
        return new Exists();
    }
}
