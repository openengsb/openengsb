package org.openengsb.ui.web.model;

import org.apache.wicket.model.IModel;
import org.openengsb.core.common.context.ContextCurrentService;

public class CurrentContextModel implements IModel<String> {

    private ContextCurrentService contextService;

    public CurrentContextModel(ContextCurrentService contextService) {
        this.contextService = contextService;
    }

    @Override
    public String getObject() {
        if (contextService == null) {
            return null;
        }
        return contextService.getThreadLocalContext();
    }

    @Override
    public void setObject(String object) {
        if (contextService != null && object != null) {
            contextService.setThreadLocalContext(object);
        }
    }

    @Override
    public void detach() {
        // do nothing
    }

}
