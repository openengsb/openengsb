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

package org.openengsb.maven.common.pojos;

/**
 * The LogLevelBuild serves to summarize the Log Level and provides for the
 * Build Engine Maven.
 * 
 */
public class LogLevelMaven {

    public final static int DEBUG = 0;
    public final static int INFO = 1;
    public final static int WARN = 2;
    public final static int ERROR = 3;
    public final static int FATAL = 4;
    public final static int QUIET = 5;

    /**
     * actual value of the log level
     */
    private int actualLevel = LogLevelMaven.INFO;

    /**
     * check it the log level is set on the warn level
     * 
     * @return true it the log level is set on the warn level, else false
     */
    public boolean isWarnEnabled() {
        if (actualLevel <= LogLevelMaven.WARN) {
            return true;
        }
        return false;
    }

    /**
     * check it the log level is set on the info level
     * 
     * @return true it the log level is set on the info level, else false
     */
    public boolean isInfoEnabled() {
        if (actualLevel <= LogLevelMaven.INFO) {
            return true;
        }
        return false;
    }

    /**
     * check it the log level is set on the error level
     * 
     * @return true it the log level is set on the error level, else false
     */
    public boolean isErrorEnabled() {
        if (actualLevel <= LogLevelMaven.ERROR) {
            return true;
        }
        return false;
    }

    /**
     * check it the log level is set on the debug level
     * 
     * @return true it the log level is set on the debug level, else false
     */
    public boolean isDebugEnabled() {
        if (actualLevel <= LogLevelMaven.DEBUG) {
            return true;
        }
        return false;
    }

    /**
     * check it the log level is set on the verbose level
     * 
     * @return true it the log level is set on the verbose level, else false
     */
    public boolean isFatalEnabled() {
        if (actualLevel <= LogLevelMaven.FATAL) {
            return true;
        }
        return false;
    }

    /**
     * check it the log level is set on the quiet level
     * 
     * @return true it the log level is set on the quiet level, else false
     */
    public boolean isQuietEnabled() {
        if (actualLevel <= LogLevelMaven.QUIET) {
            return true;
        }
        return false;
    }

    public void setActualLevel(int actualLevel) {
        this.actualLevel = actualLevel;
    }

    public int getActualLevel() {
        return actualLevel;
    }

}
