package org.openengsb.ui.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.ui.web.global.BookmarkablePageLabelLink;


// this class tests the ui for visible components depending on the logged in user roles
public class RoleAuthorizationTest extends LoginAbstract {


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

    @Test
    public void testTestClientVisibleComponentsForAdmin_EveryThingShouldBeVisible() {
        
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "admin");
        formTester.setValue("password", "password");
        formTester.submit();
        tester.assertRenderedPage(TestClient.class);
    }

}
