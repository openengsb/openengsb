package org.openengsb.ui.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.usermanagement.UserManagerImpl;
import org.openengsb.core.usermanagement.model.User;
import org.openengsb.ui.web.global.BookmarkablePageLabelLink;
import org.osgi.framework.ServiceReference;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;


// this class tests the ui for visible components depending on the logged in user roles
public class RoleAuthorizationTest extends LoginAbstract{


    private WicketTester tester;

    @Before
    public void setUp() {
        tester = getTester();
    }

    @Test
    public void testHeaderComponentsForAdmin_UserServiceShouldBeVisible() {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "admin");
        formTester.setValue("password", "password");
        formTester.submit();
        BookmarkablePageLabelLink userServiceLink = (BookmarkablePageLabelLink) tester
            .getComponentFromLastRenderedPage("header:headerMenuItems:5:link");
        assertNotNull(userServiceLink);
        assertThat(userServiceLink.getPageClass().getCanonicalName(), is(UserService.class.getCanonicalName()));
    }

    @Test
    public void testHeaderComponentsForNormalUser_UserServiceShouldNotBeVisible() {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "user");
        formTester.setValue("password", "password");
        formTester.submit();
        BookmarkablePageLabelLink userServiceLink = (BookmarkablePageLabelLink) tester
            .getComponentFromLastRenderedPage("header:headerMenuItems:5:link");
        assertNull(userServiceLink);
    }
    
}
