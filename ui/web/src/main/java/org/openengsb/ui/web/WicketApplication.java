package org.openengsb.ui.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

public class WicketApplication extends WebApplication {

    @Override
    protected void init() {
        super.init();
        addComponentInstantiationListener(new SpringComponentInjector(this));
        getResourceSettings().setAddLastModifiedTimeToResourceReferenceUrl(true);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return Index.class;
    }
}
