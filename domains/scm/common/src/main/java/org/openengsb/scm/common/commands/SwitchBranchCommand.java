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

package org.openengsb.scm.common.commands;

/**
 * Interface that defines the parameters for the switchBranch-operation with
 * setters. For the meaning of the parameters see the
 * <code>{@link CommandFactory}</code>-interface and
 * <code>{@link AbstractScmCommandFactory}</code>
 * 
 * @see CommandFactory
 * @see AbstractCommandfactory
 */
public interface SwitchBranchCommand extends Command<Object> {
    void setBranchName(String branchName);
}
