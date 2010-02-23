package org.openengsb.drools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.drools.CheckedDroolsException;
import org.drools.RuleBase;
import org.drools.compiler.RuleBaseLoader;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;

public class FileRuleSource implements RuleBaseSource {

    private String path;

    public final String getPath() {
        return this.path;
    }

    public final void setPath(String path) {
        this.path = path;
    }

    @Override
    public RuleBase getRulebase() {
        if (path == null) {
            throw new IllegalStateException("path must be set");
        }
        Resource rbaseResource = ResourceFactory.newClassPathResource(path);
        RuleBaseLoader loader = RuleBaseLoader.getInstance();
        InputStream is;

        try {
            is = rbaseResource.getInputStream();
            return loader.loadFromReader(new InputStreamReader(is));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot initialize rulebase from path " + path, e);
        } catch (CheckedDroolsException e) {
            throw new IllegalStateException("Cannot initialize rulebase from path" + path, e);
        }
    }
}
