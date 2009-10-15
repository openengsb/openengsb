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
package org.openengsb.connector.svn;

import org.openengsb.connector.svn.commands.SvnCommandFactory;

/**
 * A sub-class of the SvnCommandFactory simply to be able to call it
 * Configfuration. This is done to hide the fact that CommandFactories are used
 * and instead suggest that they are Configuration-objects (which is actually
 * not untrue). This makes an SU-writer's job potentially less confusing.
 */
public class SvnConfiguration extends SvnCommandFactory {

}
