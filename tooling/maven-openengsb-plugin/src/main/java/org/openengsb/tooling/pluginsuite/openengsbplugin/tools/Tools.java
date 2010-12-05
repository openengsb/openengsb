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

package org.openengsb.tooling.pluginsuite.openengsbplugin.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public abstract class Tools {

    public static String capitalizeFirst(String st) {
        if (st == null) {
            return null;
        } else if (st.matches("[\\s]*")) {
            return st;
        } else if (st.length() == 1) {
            return st.toUpperCase();
        } else {
            return st.substring(0, 1).toUpperCase() + st.substring(1, st.length());
        }
    }

    public static void replaceInFile(File f, String pattern, String replacement) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(f));

        String str = "";
        String line = "";

        while ((line = br.readLine()) != null) {
            str += line + "\n";
        }

        br.close();

        str = str.replaceAll(pattern, replacement);

        FileWriter fw = new FileWriter(f);
        fw.write(str);
        fw.close();

    }

}
