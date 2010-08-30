package org.openengsb.ui.web.global.header;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.ui.web.Index;
import org.openengsb.ui.web.TestClient;
import org.openengsb.ui.web.global.footer.ImprintPage;
import org.openengsb.ui.web.service.DomainService;


public class HeaderTemplateTest {
    public interface TestInterface {

        void update(String id, String name);
    }

    public class TestService implements TestInterface {

        public boolean called = false;

        @Override
        public void update(String id, String name) {
            called = true;
        }

        public String getName(String id) {
            return "";
        }

    }

    private WicketTester tester;
    private ApplicationContextMock context;

    @Before
    public void setup() {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        context.putBean(mock(ContextCurrentService.class));
    }

    @Test
    public void testNavigationFieldForIndex() {
        setupIndexPage();
        assertTrue(testNavigation(Index.class, Index.class.getSimpleName()));
    }

    @Test
    public void testNavigationFieldForTestClient() {
        setupTestClientPage();
        assertTrue(testNavigation(TestClient.class, TestClient.class.getSimpleName()));
    }

    @Test
    public void testNavigationForNonExistingNavigationButton() {
        assertTrue(testNavigation(ImprintPage.class, Index.class.getSimpleName()));
    }

    private boolean testNavigation(Class page, String expectedIndexName) {
        tester.startPage(page);
        return HeaderTemplate.getActiveIndex().equals(expectedIndexName);
    }

    private void setupTestClientPage() {
        DomainService domainServiceMock = Mockito.mock(DomainService.class);
        context.putBean(domainServiceMock);
        setupTesterWithSpringMockContext();
    }

    private void setupIndexPage() {
        DomainService domainServiceMock = Mockito.mock(DomainService.class);
        context.putBean(domainServiceMock);
        setupTesterWithSpringMockContext();
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), context, true));
    }
}
