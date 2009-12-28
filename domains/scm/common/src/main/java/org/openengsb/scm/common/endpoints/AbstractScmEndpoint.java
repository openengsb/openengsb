/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

package org.openengsb.scm.common.endpoints;

import javax.jbi.management.DeploymentException;

import org.openengsb.scm.common.commands.CommandFactory;


/**
 * An abstract ProviderEnpoint to be used by all Endpoints that supply
 * functionality for an SCM. Additional to the convenience-methods from
 * {@link AbstractEndpoint} it provides functionylity to get, set and validate a
 * CommandFactory
 * 
 */
public abstract class AbstractScmEndpoint extends AbstractEndpoint {
    private CommandFactory commandFactory = null;

    /* ProviderEndpoint overrides */

    @Override
    public void validate() throws DeploymentException {
        if (this.commandFactory == null) {
            throw new DeploymentException("missing configuration.");
        }

        this.commandFactory.validate();
    }

    /* end ProviderEndpoint overrides */

    /* getters and setters */

    public CommandFactory getCommandFactory() {
        return this.commandFactory;
    }

    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    /**
     * Fake getter for the CommandFactory to hide the fact, that a
     * CommandFactory is used and instead suggests, that it is a Configuration
     * 
     * @return the Configuration aka CommandFactory
     */
    public CommandFactory getConfiguration() {
        return getCommandFactory();
    }

    /**
     * Fake setter for the CommandFactory to hide the fact, that a
     * CommandFactory is used and instead suggests, that it is a Configuration
     * 
     * @param configuration The Configuration aka CommandFactory
     */
    public void setConfiguration(CommandFactory configuration) {
        setCommandFactory(configuration);
    }

    /* getters and setters */
}
