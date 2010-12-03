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
