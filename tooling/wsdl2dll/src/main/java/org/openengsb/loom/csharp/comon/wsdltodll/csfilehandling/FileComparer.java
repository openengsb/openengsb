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

public class FileComparer {
    private static final String CsharpClassName = "class";
    private File csFile1;
    private File csFile2;

    public FileComparer(File csFile1, File csFile2) {
        this.csFile1 = csFile1;
        this.csFile2 = csFile2;
    }

    /**
     * Removes sumalar classes in the first cs File
     * 
     * @throws IOException
     */
    public void removeSimilarClassesFromFile1() throws IOException {
        List<String> linescs1 = getFileLinesAsList(csFile1);
        List<String> linescs2 = getFileLinesAsList(csFile2);
        List<String> classNames1 = searchClass(linescs1);
        List<String> classNames2 = searchClass(linescs2);

        for (String name : findSimilarClassNames(classNames1, classNames2)) {
            linescs1 = removeClass(linescs1, name);
        }
        linescs1 = removeUselessAttributes(linescs1);
        saveFiles(linescs1, csFile1);
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
                if (lines.get(i).contains("{")) {
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
     * Returns the Classname of a line
     * 
     * @param line
     * @return
     */
    private String getClassName(String line) {
        String result = line.substring(line.indexOf(CsharpClassName)
                + CsharpClassName.length());
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
    public static void removeSimilaritiesAndSaveFiles(List<String> filepathes)
        throws IOException {
        List<File> files = new LinkedList<File>();
        for (String path : filepathes) {
            files.add(new File(path));
        }
        FileComparer fcomparer;
        for (int i = 0; i < files.size(); i++) {
            for (int y = i + 1; y < files.size(); y++) {
                fcomparer = new FileComparer(files.get(i), files.get(y));
                fcomparer.removeSimilarClassesFromFile1();
            }
        }
    }
}
