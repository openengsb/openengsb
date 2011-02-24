package org.openengsb.ui.admin.workflowEditor;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.workflow.editor.Action;
import org.openengsb.core.common.workflow.editor.WorkflowEditorService;
import org.openengsb.core.test.NullDomain;
import org.openengsb.ui.admin.model.OpenEngSBVersion;
import org.openengsb.ui.admin.workflowEditor.action.EditAction;

public class EditActionTest {

    private WicketTester tester;

    private FormTester formTester;

    private Action action;

    private ApplicationContextMock mock;

    @Before
    public void setup() {
        action = new Action();
        action.setLocation("test");
        tester = new WicketTester();
        mock = new ApplicationContextMock();
        mock.putBean(mock(ContextCurrentService.class));
        mock.putBean("openengsbVersion", new OpenEngSBVersion());
        mock.putBean("workflowEditorService", mock(WorkflowEditorService.class));
        List<DomainProvider> domainProviders = new ArrayList<DomainProvider>();
        DomainProvider provider = mock(DomainProvider.class);
        when(provider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) throws Throwable {
                return NullDomain.class;
            }
        });
        domainProviders.add(provider);
        DomainService domainServiceMock = mock(DomainService.class);
        when(domainServiceMock.domains()).thenReturn(domainProviders);
        mock.putBean("domainService", domainServiceMock);
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), mock, true));
        tester.startPage(new EditAction(action));
        formTester = tester.newFormTester("actionForm");
    }

    @Test
    public void editForm_shouldUpdateAction() {
        String location = "location";
        assertThat(action.getLocation(), equalTo(formTester.getTextComponentValue(location)));
        tester.dumpPage();
        formTester.setValue(location, location);
        formTester.select("domainSelect", 0);
        formTester.submit();
        formTester = tester.newFormTester("actionForm");
        formTester.select("methodSelect", 1);
        formTester.submit();
        tester.assertRenderedPage(WorkflowEditor.class);
        assertThat(action.getLocation(), equalTo(location));
        assertEquals(action.getDomain(), NullDomain.class);
        assertThat(action.getMethodName(), equalTo(NullDomain.class.getMethods()[1].getName()));
    }
}
