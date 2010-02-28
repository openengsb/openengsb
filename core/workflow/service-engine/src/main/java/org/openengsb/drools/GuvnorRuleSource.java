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
package org.openengsb.drools;

import java.util.Properties;

import org.drools.RuleBase;
import org.drools.agent.RuleAgent;

public class GuvnorRuleSource implements RuleBaseSource {

    private String url;

    public final String getUrl() {
        return this.url;
    }

    public final void setUrl(String url) {
        this.url = url;
    }

    @Override
    public RuleBase getRulebase() {
        Properties config = new Properties();
        config.put("url", this.url);
        RuleAgent agent = RuleAgent.newRuleAgent(config);
        return agent.getRuleBase();
    }
}
