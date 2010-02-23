package org.openengsb.drools;

import java.util.Properties;

import org.drools.RuleBase;
import org.drools.agent.RuleAgent;

public class GuvnorRuleSource implements RuleBaseSource {

    private static final String GUVNOR_DEFAULT_URL = "http://localhost:8081/drools-guvnor/org.drools.guvnor.Guvnor/package/org.openengsb/LATEST";
    private String url;

    public GuvnorRuleSource() {
        this(GUVNOR_DEFAULT_URL);
    }

    public GuvnorRuleSource(String url) {
    }

    public final String getUrl() {
        return this.url;
    }

    public final void setUrl(String url) {
        this.url = url;
    }

    @Override
    public RuleBase getRulebase() {
        Properties config = new Properties();
        config.put("url", url);
        RuleAgent agent = RuleAgent.newRuleAgent(config);
        return agent.getRuleBase();
    }
}
