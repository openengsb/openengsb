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
import java.util.regex.Pattern;

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

    public FileComparer(Log logger) {
        this.logging = logger;
    }

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
        List<String> classNames1 = searchClasses(linescs1);
        logging.info("Found " + classNames1.size() + " classes");
        logging.info("Search classes");
        List<String> classNames2 = searchClasses(linescs2);
        logging.info("Found " + classNames2.size() + " classes");
        logging.info("Removing similarities from the file");
        for (String name : findSimilarClassNames(classNames1, classNames2)) {
            linescs1 = removeLinesContainingClassname(linescs1, name);
        }
        logging.info("Remove Attributes, which stands alone");
        linescs1 = removeAttributesNotBoundToClass(linescs1);
        if (!windows) {
            logging.info("Replace abstract classes with interfaces");
            linescs1 = replaceAbstractClasses(linescs1);
            linescs2 = replaceAbstractClasses(linescs2);
            logging.info("Save files");
        }
        replaceFilesWithNewContent(linescs1, csFile1);
        replaceFilesWithNewContent(linescs2, csFile2);
    }

    /**
     * Searches for abstract classes and replace it by an interface.
     * 
     * @param lines
     * @return
     */
    public List<String> replaceAbstractClasses(List<String> lines) {
        List<String> result = new LinkedList<String>();
        for (int i = 0; i < lines.size(); i++) {
            result.add(removeAbstract(lines.get(i)));
        }
        return result;
    }

    private String removeAbstract(String line) {
        String tmpLine = line;
        if (isAbstractClassLine(tmpLine)) {
            logging.info("Found abstract class. Convert it to a interface");
            tmpLine = tmpLine.replace(ABSTRACT_CLASS_SIGNITURE + " ", "interface I");
            if (tmpLine.contains(":")) {
                tmpLine = tmpLine.substring(0, tmpLine.indexOf(":")) + "{";
            }
            tmpLine = tmpLine.replace(" {", "SoapBinding {");
        } else {
            if (tmpLine.contains("abstract")) {
                logging.info("Found abstract method. Convert it");
                tmpLine = tmpLine.replace("abstract", "");
                tmpLine = tmpLine.replace("public", "");
                tmpLine = tmpLine.replace("private", "");
                tmpLine = tmpLine.replace("protected", "");
            }
        }
        return tmpLine;
    }

    private boolean isAbstractClassLine(String line) {
        return line.contains(ABSTRACT_CLASS_SIGNITURE);
    }

    public void replaceFilesWithNewContent(List<String> lines, File file) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < lines.size(); i++) {
            out.write(lines.get(i));
            out.newLine();
        }
        out.close();
    }

    public List<String> removeLinesContainingClassname(List<String> lines, String className) {
        List<String> result = new LinkedList<String>();
        boolean ignoreLine = false;
        int openingBracket = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (containsLineClassname(lines.get(i), className) && isCSharpClass(lines.get(i))) {
                ignoreLine = true;
            }
            if (!ignoreLine) {
                result.add(lines.get(i));
            } else {
                if (lines.get(i).contains("{")) {
                    openingBracket++;
                }
                if (lines.get(i).contains("}")) {
                    openingBracket--;
                }
                if (openingBracket <= 0) {
                    result.add("");
                    ignoreLine = false;
                    openingBracket = 0;
                }
            }
        }
        return result;
    }

    private boolean containsLineClassname(String line, String classname) {
        String classline = line;
        if (line.contains(":")) {
            classline = line.substring(0, line.indexOf(":"));
        }
        String patternString = "\\b(" + classname + ")\\b";
        Pattern pattern = Pattern.compile(patternString);
        return pattern.matcher(classline).find();
    }

    public List<String> removeAttributesNotBoundToClass(List<String> lines) {
        List<String> linesWithoutUnboundAttributes = new LinkedList<String>();
        List<Integer> tmpLineIndexToDelete = new LinkedList<Integer>();
        List<Integer> linesToDelete = new LinkedList<Integer>();
        boolean addAttributesToDelete = false;
        for (int i = 0; i < lines.size(); i++) {
            linesWithoutUnboundAttributes.add(lines.get(i));
            if (lines.get(i).contains("[")) {
                addAttributesToDelete = true;
            }
            if (addAttributesToDelete) {
                tmpLineIndexToDelete.add(i);
                if (lines.get(i).contains("{")
                        || (lines.get(i) != "" && !lines.get(i).contains("["))) {
                    addAttributesToDelete = false;
                    tmpLineIndexToDelete = new LinkedList<Integer>();
                    continue;
                } else {
                    if (lines.get(i) == "") {
                        linesToDelete.addAll(tmpLineIndexToDelete);
                        addAttributesToDelete = false;
                        tmpLineIndexToDelete = new LinkedList<Integer>();
                    }
                }
            }
        }
        return removeLines(linesWithoutUnboundAttributes, linesToDelete);
    }

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
     * Finds all the classes in a File (lines=read File line by line)
     * 
     * @param lines
     * @return
     */
    private List<String> searchClasses(List<String> lines) {
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
    public List<String> getFileLinesAsList(File f) throws IOException {
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
