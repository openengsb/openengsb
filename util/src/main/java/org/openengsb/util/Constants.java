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

package org.openengsb.util;

public class Constants {
    public static enum OS {
        UNIX, OSX, WINDOWS
    }

    public static final String OS_NAME = System.getProperty("os.name");

    public static final OS OS_TYPE;

    static {
        String os = Constants.OS_NAME.toLowerCase().trim();
        if (os.startsWith("mac") || os.startsWith("darwin")) {
            OS_TYPE = OS.OSX;
        } else if (os.startsWith("windows")) {
            OS_TYPE = OS.WINDOWS;
        } else {
            OS_TYPE = OS.UNIX;
        }
    }

    public static final boolean IS_OSX = Constants.OS_TYPE.equals(OS.OSX);
    public static final boolean IS_WINDOWS = Constants.OS_TYPE.equals(OS.WINDOWS);
    public static final boolean IS_UNIX = !Constants.IS_WINDOWS && !Constants.IS_OSX;

    public static final String FILE_WILDECARD = Constants.IS_WINDOWS ? "*.*" : "*";

    public static final String LINE_SEPARATOR;

    static {
        String sep = System.getProperty("line.separator");
        LINE_SEPARATOR = sep == null || sep.length() == 0 ? "\n" : sep;
    }

    private Constants() {
        throw new AssertionError();
    }
}
