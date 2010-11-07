package org.openengsb.ui.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.ui.web.global.footer.FooterTemplate;
import org.openengsb.ui.web.global.header.HeaderTemplate;

public class BasePanel extends Panel {

    @SpringBean
    private ContextCurrentService contextService;

    public BasePanel(String id) {
        super(id);
        initContextForCurrentThread();
        initWebPage();
    }

    private void initWebPage() {

        Form<?> form = new Form<Object>("projectChoiceForm");
        form.add(createProjectChoice());
        add(form);
        form.add(new Link<Object>("logout") {
            @Override
            public void onClick() {
                ((AuthenticatedWebSession) this.getSession()).signOut();
                setResponsePage(LoginPage.class);
            }
        });

    }

    private Component createProjectChoice() {
        DropDownChoice<String> dropDownChoice = new DropDownChoice<String>("projectChoice", new IModel<String>() {
            public String getObject() {
                return getSessionContextId();
            }

            public void setObject(String object) {
                setThreadLocalContext(object);
            }

            public void detach() {
            }
        }, getAvailableContexts()) {
            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }

            @Override
            protected void onModelChanged() {
                setResponsePage(BasePage.class);
            }

        };
        return dropDownChoice;
    }

    private List<String> getAvailableContexts() {
        if (contextService == null) {
            return new ArrayList<String>();
        }
        return contextService.getAvailableContexts();
    }

    final void initContextForCurrentThread() {
        String sessionContextId = getSessionContextId();
        try {
            if (contextService != null) {
                contextService.setThreadLocalContext(sessionContextId);
            }
        } catch (IllegalArgumentException e) {
            contextService.createContext(sessionContextId);
            contextService.createContext(sessionContextId + "2");
            contextService.setThreadLocalContext(sessionContextId);
            contextService.putValue("domain/NotificationDomain/defaultConnector/id", "notification");
            contextService.putValue("domain/IssueDomain/defaultConnector/id", "issue");
            contextService.putValue("domain/ExampleDomain/defaultConnector/id", "example");
        }
    }

     public String getSessionContextId() {
        WicketSession session = WicketSession.get();
        if (session == null) {
            return "foo";
        }
        if (session.getThreadContextId() == null) {
            setThreadLocalContext("foo");
        }
        return session.getThreadContextId();
    }

     private void setThreadLocalContext(String threadLocalContext) {
        WicketSession session = WicketSession.get();
        if (session != null) {
            session.setThreadContextId(threadLocalContext);
        }
    }
}
