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
package org.openengsb.drools.source.dir;

import java.io.File;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.source.DirectoryRuleSource;

public class ReloadChecker extends TimerTask {

	private Log log = LogFactory.getLog(getClass());
    protected File file;
    protected DirectoryRuleSource ruleSource;

    public ReloadChecker(File file, DirectoryRuleSource ruleSource) {
        super();
        this.file = file;
        this.ruleSource = ruleSource;
    }

    @Override
    public void run() {
        if (file.exists()) {
        	log.info("Find rulebase file. Forcing rulebase to reload");
            try {
                ruleSource.readRuleBase();
                log.info("Rulebase reloaded successfully.");
            } catch (RuleBaseException e) {
                log.error("Reload of rulebase is not possible. Please correct the error and recreate the reload file.",e);
            }finally {
            	log.info("Attempt to reload rulebase finished. Remvoing rulebase reload file.");
            	file.delete();
            }
        }
        log.trace("Rulebase reload file does not exist.");
    }

}
