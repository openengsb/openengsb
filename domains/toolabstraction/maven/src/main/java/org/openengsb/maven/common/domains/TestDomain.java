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

package org.openengsb.maven.common.domains;

import java.io.File;
import java.util.List;

import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.result.MavenResult;


/**
 * The interface describes the possibilities to configure the test domain.
 * 
 */
public interface TestDomain {

    /**
     * executes the Tests
     * 
     * @throws MavenException
     */
    MavenResult executeTests() throws MavenException;

    /**
     * get the depended Files from the Classpath </br> not implemented or used
     * at the moment
     * 
     * @return as a List of Files
     */
    List<File> getClasspath();

    /**
     * includes the given tests to the execution default, all tests are included
     * 
     * @param includes - tests that should be executed
     */
    void setIncludes(String... includes);

    /**
     * Supplies the tests, that are included for the run
     * 
     * @return includes - all Tests that should be run
     */
    String[] getIncludes();

    /**
     * excludes the given tests from the execution default: all tests are
     * included
     * 
     * @param excludes - tests that should not be executed
     */
    void setExcludes(String... excludes);

    /**
     * Supplies the tests, that are excluded for the run
     * 
     * @return excludes - all Tests that should not be run
     */
    String[] getExcludes();

    /**
     * Sets the directory for the test classes
     * 
     * @param testClassesDir as a File
     */
    void setTestClassesDir(File testClassesDir);

    /**
     * Returns the directory for the test classes
     * 
     * @return as a File
     */
    File getTestClassesDir();

    /**
     * Sets the destination where the report of the test should be placed
     * 
     * @param testResultsDir the destination as a File Object
     */
    void setTestReportDir(File testReportDir);

    /**
     * Supplies the destination where the report of the test should be placed
     * 
     * @return the destination as a File Object
     */
    File getTestReportDir();

    /**
     * Sets whether it should throw an exception in case of test failure or
     * error.
     * 
     * @param stopAtFailuresOrErrors - true to stop at failure or error
     */
    void setStopAtFailuresOrErrors(boolean stopAtFailuresOrErrors);

    /**
     * Returns whether it should throw an exception in case of test failure or
     * error.
     * 
     * @return true if a stop should be prepared at failure or error
     */
    boolean isStopAtFailuresOrErrors();

    /**
     * Enables the print of a testreport or not if true, a testreport will be
     * printed
     * 
     * @param testReport
     */
    void setTestReport(boolean testReport);

    /**
     * Returns if a testreport should be printed or not
     * 
     * @return testreport - if true, print a testreport, else not
     */
    boolean isTestReport();

    /**
     * Sets the baseDirectory of the project, that should be tested </br> It is
     * required to get the directory of the pom.xml
     * 
     * After the baseDirectory is set, the dom is generated to manipulate it and
     * use the other options
     * 
     * @param baseDirectory - the file to set the project place
     * @throws MavenException if there is no pom.xml in this directory
     */
    void setBaseDirectory(File baseDirectory) throws MavenException;

    /**
     * Supplies the baseDirectory of the project, that should be tested
     * 
     * @return baseDirectory - as a File for initialize the the project
     */
    File getBaseDirectory();

    /**
     * Sets a flag to skip the tests
     * 
     * @param skipTests
     */
    void setSkipTests(boolean skipTests);

    /**
     * Return the actual value of skipping tests
     * 
     * @return true if the Tests should be skipped
     */
    boolean isSkipTests();

    /**
     * Sets the test source directories
     * 
     * @param testSrcDir directories that are supposed to run
     */
    void setTestSrcDirs(File testSrcDir);

    /**
     * Returns a List of Files which are included in the test source directories
     * 
     * @return the test source directories
     */
    File getTestSrcDirs();

}
