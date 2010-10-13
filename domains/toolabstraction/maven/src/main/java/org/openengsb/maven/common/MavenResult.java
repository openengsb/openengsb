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

package org.openengsb.maven.common;

import java.util.HashMap;
import java.util.Map;

/**
 * The BuildResult represents the possible values, that the build engine
 * provides. The values of Maven are Error, Failure and Success.
 *
 * Additional Properties like timestamp, the failure task or the exception list
 * should describe the Output of the Build Engine.
 *
 */
public class MavenResult {

    public static final String ERROR = "Error";
    public static final String FAILURE = "Failure";
    public static final String SUCCESS = "Success";

    private long timestamp;
    private String result;
    private String task;
    private String errorMessage;
    private String output;

    private String file;
    private String[] deployedFiles;

    private Map<String, byte[]> testReports;

    public String getFile() {
        return this.file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String[] getDeployedFiles() {
        return this.deployedFiles;
    }

    public void setDeployedFiles(String[] deployedFiles) {
        this.deployedFiles = deployedFiles;
    }

    /**
     * Sets the time stamp, when the build finished
     *
     * @param timestamp - time stamp when the build finished
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Return the time when the build was finished
     *
     * @return timestamp - time when the build was finished
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * Sets the name of the failed task
     *
     * @param task - name of the failed task
     */
    public void setTask(String task) {
        this.task = task;
    }

    /**
     * Returns the name of the failed task, else null
     *
     * @return task - the name of the failed task
     */
    public String getTask() {
        return this.task;
    }

    /**
     * Sets the result of the maven process
     *
     * @param result - possible values are error, failure, success
     */
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Returns the result of the maven process
     *
     * @return result - possible values are error, failure, success
     */
    public String getResult() {
        return this.result;
    }

    /**
     * Sets a short description of the error message
     *
     * @param errorMessage - short description
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns a short description of the error message
     *
     * @return errorMessage
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(this.result);
    }

    public void setTestReports(Map<String, byte[]> testReports) {
        this.testReports = testReports;
    }

    public Map<String, byte[]> getTestReports() {
        if (this.testReports == null) {
            return new HashMap<String, byte[]>();
        }
        return testReports;
    }

}
