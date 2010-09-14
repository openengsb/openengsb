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

package org.openengsb.util;

import java.io.File;

public class WorkingDirectory {

    private static final String BASE = "data/openengsb/";
    private static final File base;

    static {
        base = new File(BASE);
        base.mkdirs();
    }

    private WorkingDirectory() {
        throw new AssertionError();
    }

    public static File getDirectory(String component) {
        File dir = new File(base, component);
        dir.mkdirs();
        return dir;
    }

    public static File getFile(String component, String file) {
        return new File(getDirectory(component), file);
    }
}
