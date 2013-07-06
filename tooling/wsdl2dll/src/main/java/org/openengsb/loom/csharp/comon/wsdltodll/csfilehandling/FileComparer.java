/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.loom.csharp.comon.wsdltodll.csfilehandling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;
// Note: Under Linux, WSDL and WSDL2 converts a WSDL not to an interface. It creates an abstract class that
// is not compatible with the .Net Bridge (RealProxy only accepts interface or Classes that implement 
// MarshalByRefObject). It follows that the abstract classes have to be replaced by interfaces.
// To be compatible with wsdl.exe (windows) the interface names are converted to I+"NAME"+SoapBinding

public class FileComparer {
    private static final String ABSTRACT_CLASS_SIGNITURE = "abstract partial class";
    private static final String CSHARP_CLASS_NAME = "class";
    private File csFile1;
    private File csFile2;
    private Boolean windows;
    private Log logging;

    public FileComparer(File csFile1, File csFile2, Log logging,
            Boolean windows) {
        this.csFile1 = csFile1;
        this.csFile2 = csFile2;
        this.windows = windows;
        this.logging = logging;
    }

    /**
     * Removes similar classes in the first cs File
     * 
     * @throws IOException
     */
    public void removeSimilarClassesFromFile1() throws IOException {
        logging.info("Start reding cs File");
        List<String> linescs1 = getFileLinesAsList(csFile1);
        logging.info("Start reding cs File");
        List<String> linescs2 = getFileLinesAsList(csFile2);
        logging.info("Search classes");
        List<String> classNames1 = searchClass(linescs1);
        logging.info("Found " + classNames1.size() + " classes");
        logging.info("Search classes");
        List<String> classNames2 = searchClass(linescs2);
        logging.info("Found " + classNames2.size() + " classes");
        logging.info("Removing similarities from the file");
        for (String name : findSimilarClassNames(classNames1, classNames2)) {
            linescs1 = removeClass(linescs1, name);
        }
        logging.info("Remove Attributes, which stands alone");
        linescs1 = removeUselessAttributes(linescs1);
        if (!windows) {
            logging.info("Replace abstract classes with interfaces");
            linescs1 = replaceAbstractClasses(linescs1);
            linescs2 = replaceAbstractClasses(linescs2);
            logging.info("Save files");
        }
        saveFiles(linescs1, csFile1);
        saveFiles(linescs2, csFile2);
    }

    /**
     * Searches for abstract classes and replace it by an interface.
     * 
     * @param lines
     * @return
     */
    private List<String> replaceAbstractClasses(List<String> lines) {
        List<String> result = new LinkedList<String>();
        for (int i = 0; i < lines.size(); i++) {
            result.add(removeAbstract(lines.get(i)));
        }
        return result;
    }

    /**
     * Replace the abstract class with interface
     * 
     * @param line
     * @return
     */
    private String removeAbstract(String line) {
        String tmp = line;
        if (isAbstractClassLine(tmp)) {
            logging.info("Found abstract class. Convert it to a interface");
            tmp = tmp.replace(ABSTRACT_CLASS_SIGNITURE + " ", "interface I");
            if (tmp.contains(":")) {
                tmp = tmp.substring(0, tmp.indexOf(":")) + "{";
            }
            tmp = tmp.replace(" {", "SoapBinding {");
        } else {
            if (tmp.contains("abstract")) {
                logging.info("Found abstract method. Convert it");
                tmp = tmp.replace("abstract", "");
                tmp = tmp.replace("public", "");
                tmp = tmp.replace("private", "");
                tmp = tmp.replace("protected", "");
            }
        }
        return tmp;
    }

    /**
     * Checks if a line is a abstract class
     * 
     * @param line
     * @return
     */
    private boolean isAbstractClassLine(String line) {
        return line.contains(ABSTRACT_CLASS_SIGNITURE);
    }

    /**
     * Saves the file with the new content
     * 
     * @param lines
     * @param file
     * @throws IOException
     */
    private void saveFiles(List<String> lines, File file) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < lines.size(); i++) {
            out.write(lines.get(i));
            out.newLine();
        }
        out.close();
    }

    /**
     * removes the class with the name in className from the File lines
     * 
     * @param lines
     * @param className
     * @return
     */
    private List<String> removeClass(List<String> lines, String className) {
        List<String> result = new LinkedList<String>();
        boolean ignore = false;
        int openingKlammer = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (isClass(lines.get(i), className) && isCSharpClass(lines.get(i))) {
                ignore = true;
            }
            if (!ignore) {
                result.add(lines.get(i));
            } else {
                if (lines.get(i).contains("{")) {
                    openingKlammer++;
                }
                if (lines.get(i).contains("}")) {
                    openingKlammer--;
                }
                if (openingKlammer <= 0) {
                    result.add("");
                    ignore = false;
                    openingKlammer = 0;
                }
            }
        }
        return result;
    }

    /**
     * Checks if a String line is the first line of a class
     * 
     * @param line
     * @param classname
     * @return
     */
    private boolean isClass(String line, String classname) {
        String classline = line;
        if (line.contains(":")) {
            classline = line.substring(0, line.indexOf(":"));
        }
        return classline.contains(classname);
    }

    /**
     * This method removes the Attributes that are not bind to a class
     * 
     * @param lines
     * @return
     */
    private List<String> removeUselessAttributes(List<String> lines) {
        List<String> result = new LinkedList<String>();
        List<Integer> tmp = new LinkedList<Integer>();
        List<Integer> linesToDelete = new LinkedList<Integer>();
        boolean count = false;
        for (int i = 0; i < lines.size(); i++) {
            result.add(lines.get(i));
            if (lines.get(i).contains("[")) {
                count = true;
            }
            if (count) {
                tmp.add(i);
                if (lines.get(i).contains("{")
                        || (lines.get(i) != "" && !lines.get(i).contains("["))) {
                    count = false;
                    tmp = new LinkedList<Integer>();
                    continue;
                } else {
                    if (lines.get(i) == "") {
                        linesToDelete.addAll(tmp);
                        count = false;
                        tmp = new LinkedList<Integer>();
                    }
                }
            }
        }
        return removeLines(result, linesToDelete);
    }

    /**
     * Removes Elements from a list
     * 
     * @param input
     * @param removeLinesAsInteger
     * @return
     */
    private List<String> removeLines(List<String> input,
            List<Integer> removeLinesAsInteger) {
        Collections.sort(removeLinesAsInteger, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return -(o1 - o2);
            }
        });
        List<String> result = new LinkedList<String>(input);
        for (int line : removeLinesAsInteger) {
            result.remove(line);
        }
        return result;

    }

    /**
     * Search for similar class names
     * 
     * @param classnames1
     * @param classnames2
     * @return
     */
    private List<String> findSimilarClassNames(List<String> classnames1,
            List<String> classnames2) {
        List<String> result = new LinkedList<String>();
        for (String name : classnames2) {
            if (classnames1.contains(name)) {
                result.add(name);
            }
        }
        return result;
    }

    /**
     * Finds alle the classes in a File (lines=read File line by line)
     * 
     * @param lines
     * @return
     */
    private List<String> searchClass(List<String> lines) {
        List<String> result = new LinkedList<String>();
        for (String line : lines) {
            if (isCSharpClass(line)) {
                result.add(getClassName(line));
            }
        }
        return result;
    }

    /**
     * Returns the Class name of a line
     * 
     * @param line
     * @return
     */
    private String getClassName(String line) {
        String result = line.substring(line.indexOf(CSHARP_CLASS_NAME)
                + CSHARP_CLASS_NAME.length());
        if (result.contains("{")) {
            result = result.substring(0, result.indexOf("{"));
        }
        if (result.contains(":")) {
            result = result.substring(0, result.indexOf(":"));
        }
        return result.replaceAll("\\s", "");
    }

    /**
     * Checks if the line is a C# class
     * 
     * @param line
     * @return
     */
    private boolean isCSharpClass(String line) {
        return line.contains("class") && line.contains("{");
    }

    /**
     * Reads a file and returns the content as List
     * 
     * @param f
     * @return
     * @throws IOException
     */
    private List<String> getFileLinesAsList(File f) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(
            new DataInputStream(new FileInputStream(f))));
        List<String> result = new LinkedList<String>();
        String strLine;
        while ((strLine = br.readLine()) != null) {
            result.add(strLine);
        }
        br.close();
        return result;

    }

    /**
     * Removes all the similar parts from all the files
     * 
     * @param filepathes
     * @throws IOException
     */
    public static void removeSimilaritiesAndSaveFiles(List<String> filepathes,
            Log logging, Boolean isWindows) throws IOException {
        List<File> files = new LinkedList<File>();
        for (String path : filepathes) {
            files.add(new File(path));
        }
        FileComparer fcomparer;
        for (int i = 0; i < files.size(); i++) {
            for (int y = i + 1; y < files.size(); y++) {
                fcomparer = new FileComparer(files.get(i), files.get(y),
                    logging, isWindows);
                fcomparer.removeSimilarClassesFromFile1();
            }
        }
    }
}
